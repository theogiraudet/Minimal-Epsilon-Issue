package net.gunivers.dispenser.observer.extractor;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import net.gunivers.dispenser.observer.NbtPair;
import net.gunivers.dispenser.observer.NbtType;
import net.gunivers.dispenser.observer.PartialEntityType;
import net.gunivers.dispenser.observer.model.EntityType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class EntityTypeExtractor implements Extractor<EntityType> {

    private static final String VARIABLE_NAME = "nbt";
    private static final String METHOD_START_WITH = "put";
    private static final String ENTITY_REGISTRY_CLASS = "EntityType";
    private static final String REGISTER_METHOD_NAME = "register";

    private static final String[] WRITE_NBT_METHOD_NAMES = {"writeNbt", "writeCustomDataToNbt" };

    private final Map<String, EntityType> entityTypes;
    private final Map<String, String> files;

    public EntityTypeExtractor() {
        this.entityTypes = new HashMap<>();
        this.files = new HashMap<>();
        StaticJavaParser.setConfiguration(new ParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17_PREVIEW));
    }

    @Override
    public Collection<EntityType> extractFrom(Path folder) throws IOException {
        final var file = folder.toFile();
        if (file.exists()) {
            if (file.isDirectory()) {
                FileUtils.listFiles(file, new String[]{"java"}, true)
                        .forEach(f -> files.put(f.getName().replace(".java", ""), f.getAbsolutePath()));

                final var set = getEntityTypes().stream().collect(Collectors.toMap(PartialEntityType::className, t -> t));
                set.values().forEach(value -> getNbt(value, set));
                return new HashSet<>(entityTypes.values());
            }
        }
        return Set.of();
    }

    private Optional<EntityType> getNbt(PartialEntityType type, Map<String, PartialEntityType> allEntityTypes) {
        if (!entityTypes.containsKey(type.className())) {
            final Optional<ClassOrInterfaceDeclaration> clazzOpt;
            try {
                clazzOpt = StaticJavaParser
                        .parse(fileToCode(this.files.get(type.className())))
                        .getClassByName(type.className());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if(clazzOpt.isPresent()) {
                final var clazz = clazzOpt.get();
                Optional<EntityType> parent = getParentNbt(clazz, allEntityTypes);
                final var nbtPairs = new HashMap<String, NbtType>();
                for(String methodName : WRITE_NBT_METHOD_NAMES) {
                    clazzOpt.map(clazz1 -> clazz1.getMethodsByName(methodName))
                            .filter(list -> !list.isEmpty())
                            .map(list -> list.get(0))
                            .flatMap(MethodDeclaration::getBody)
                            .map(body -> getNbt(body, clazz::getFieldByName))
                            .ifPresent(nbtPairs::putAll);
                }
                final var entityType = buildEntityType(type, parent, nbtPairs);
                this.entityTypes.put(entityType.className(), entityType);
                return Optional.of(entityType);
            }
        }
        return Optional.ofNullable(this.entityTypes.get(type.className()));
    }

    private EntityType buildEntityType(PartialEntityType partialInfos, Optional<EntityType> parent, Map<String, NbtType> pairs) {
        final Map<String, EntityType.NbtTypeValue> map =
                pairs.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> new EntityType.NbtTypeString(nbtTypeToString(entry.getValue()))));
        return switch(partialInfos) {
            case PartialEntityType.PartialConcreteEntityType infos -> new EntityType.ConcreteEntityType(parent, infos.className(), infos.id(), map);
            case PartialEntityType.PartialAbstractEntityType infos -> new EntityType.AbstractEntityType(parent, infos.className(), map);
        };
    }

    private Optional<EntityType> getParentNbt(ClassOrInterfaceDeclaration clazz, Map<String, PartialEntityType> allEntityTypes) {
        for(ClassOrInterfaceType extendedType : clazz.getExtendedTypes()) {
            final var typeName = extendedType.getName().asString();
            if (!allEntityTypes.containsKey(typeName)) {
                return getNbt(new PartialEntityType.PartialAbstractEntityType(typeName), allEntityTypes);
            } else {
                return getNbt(allEntityTypes.get(typeName), allEntityTypes);
            }
        }
        return Optional.empty();
    }

    private Map<String, NbtType> getNbt(BlockStmt block, Function<String, Optional<FieldDeclaration>> fieldAccessor) {
        final var map = new HashMap<String, NbtType>();
        for (var statement : block.getStatements())
            map.putAll(getNbtFromStatement(statement, fieldAccessor));
        return map;
    }

    private Map<String, NbtType> getNbtFromStatement(Statement stmt, Function<String, Optional<FieldDeclaration>> fieldAccessor) {
        final var map = new HashMap<String, NbtType>();
        if (stmt.isBlockStmt()) {
            map.putAll(getNbt(stmt.asBlockStmt(), fieldAccessor));
        } else if (stmt.isTryStmt()) {
            map.putAll(getNbt(stmt.asTryStmt().getTryBlock(), fieldAccessor));
        } else if (stmt.isIfStmt()) {
            map.putAll(getNbtFromStatement(stmt.asIfStmt().getThenStmt(), fieldAccessor));
            stmt.asIfStmt().getElseStmt().ifPresent(st -> map.putAll(getNbtFromStatement(st, fieldAccessor)));
        } else if (stmt.isExpressionStmt()) {
            getNbtFromExpression(stmt.asExpressionStmt().getExpression(), fieldAccessor).ifPresent(pair -> map.put(pair.key(), pair.type()));
        }
        return map;
    }

    private Optional<NbtPair> getNbtFromExpression(Expression expr, Function<String, Optional<FieldDeclaration>> fieldAccessor) {
        if (expr.isMethodCallExpr()) {
            final var methodCall = expr.asMethodCallExpr();
            final var scopeName = methodCall.getScope().map(Node::toString);
            if (scopeName.map(name -> name.equals(VARIABLE_NAME)).orElse(false)) {
                final var methodName = methodCall.getName().asString();
                if (methodName.startsWith(METHOD_START_WITH)) {
                    var rawNbtType = methodName.substring(METHOD_START_WITH.length());
                    if(rawNbtType.equals("") && methodCall.getArgument(1).isMethodCallExpr()) {
                        final var secondArgument = methodCall.getArgument(1).asMethodCallExpr();
                        if(secondArgument.getName().asString().equals("toNbtList"))
                            rawNbtType = "List";
                        else
                            rawNbtType = "Compound";
                    }
                    final var nbtType = NbtType.getNbtType(rawNbtType);
                    final var firstArgument = methodCall.getArgument(0);
                    if (firstArgument.isNameExpr()) {
                        return getFieldValue(firstArgument.asNameExpr().getNameAsString(), fieldAccessor)
                                .map(key -> new NbtPair(key, nbtType));
                    } else if (firstArgument.isStringLiteralExpr()) {
                        return Optional.of(new NbtPair(firstArgument.asStringLiteralExpr().getValue(), nbtType));
                    }
                }
            }
        }
        return Optional.empty();
    }

    private Optional<String> getFieldValue(String fieldName, Function<String, Optional<FieldDeclaration>> fieldAccessor) {
        return fieldAccessor.apply(fieldName)
                .flatMap(field -> field.getVariables()
                        .stream()
                        .filter(variable -> variable.getName().asString().equals(fieldName))
                        .findFirst())
                .flatMap(variable -> variable.getInitializer().filter(Expression::isStringLiteralExpr))
                .map(variable -> variable.asStringLiteralExpr().getValue());
    }

    private Set<PartialEntityType> getEntityTypes() throws IOException {
        CompilationUnit compilationUnit = StaticJavaParser.parse(fileToCode(this.files.get(ENTITY_REGISTRY_CLASS)));
        return extractEntityTypes(compilationUnit);
    }

    private Set<PartialEntityType> extractEntityTypes(CompilationUnit compilationUnit) {
        final Set<PartialEntityType> types = new HashSet<>();
        final var clazzOpt = compilationUnit.getClassByName(ENTITY_REGISTRY_CLASS);
        if (clazzOpt.isPresent()) {
            final var clazz = clazzOpt.get();
            clazz.getFields().forEach(field -> types.addAll(extractEntityTypeFromField(field)));
        }
        return types;
    }

    private Set<PartialEntityType> extractEntityTypeFromField(FieldDeclaration field) {
        final Set<PartialEntityType> types = new HashSet<>();
        for (VariableDeclarator variable : field.getVariables()) {
            final var type = variable.getType();
            if (type.isClassOrInterfaceType() && type.asClassOrInterfaceType().getName().asString().equals(ENTITY_REGISTRY_CLASS)) {
                final var typeArguments = type.asClassOrInterfaceType().getTypeArguments();
                final var initializer = variable.getInitializer();
                if (typeArguments.isPresent() && initializer.isPresent() && initializer.get().isMethodCallExpr()) {
                    final var typeName = typeArguments.get().get(0).toString();
                    final var methodCall = initializer.get().asMethodCallExpr();
                    if (methodCall.getName().asString().equals(REGISTER_METHOD_NAME)) {
                        types.add(new PartialEntityType.PartialConcreteEntityType(typeName, StringUtils.unwrap(methodCall.getArgument(0).toString(), "\"")));
                    }
                }

            }
        }
        return types;
    }

    private String fileToCode(String path) throws IOException {
        final String[] patterns = {
                "(?<=\\()[^\\\\(\\[<]+? super [^ ]+ ",
                "\\*\\*.*",
                "(?<=lbl)-\\d+"
        };
        final var pattern = Arrays.stream(patterns).map(ptrn -> "(" + ptrn + ")").collect(Collectors.joining("|"));
        var result = Files.readString(Path.of(path)).replaceAll(pattern, "");
        final var matcher = Pattern.compile("^(\\s*)void ([a-zA-Z0-9-_]+)++(?!\\()", Pattern.MULTILINE).matcher(result);
        result = matcher.replaceAll(m -> m.group(1) + "int " + m.group(2));
        return result;
    }

    private String nbtTypeToString(NbtType nbtType) {
        return Pattern.compile("(?:^|_)([a-z])")
                .matcher(nbtType.name().toLowerCase())
                .replaceAll(m -> m.group(1).toUpperCase())
                .replace("_", "");
    }
}
