package net.gunivers.dispenser.observer;

public sealed interface PartialEntityType permits PartialEntityType.PartialAbstractEntityType, PartialEntityType.PartialConcreteEntityType {

    String className();

    record PartialConcreteEntityType(String className, String id) implements PartialEntityType {}
    record PartialAbstractEntityType(String className) implements PartialEntityType {}

}
