package net.gunivers.dispenser.observer.model;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.emfatic.core.EmfaticResource;
import org.eclipse.emf.emfatic.core.generator.emfatic.EmfaticGenerator;
import org.eclipse.epsilon.common.util.StringProperties;
import org.eclipse.epsilon.emc.emf.EmfModel;
import org.eclipse.epsilon.eol.launch.EolRunConfiguration;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ModelGenerator {

    public static void main(String... args) throws URISyntaxException {
        Path root = Paths.get(ModelGenerator.class.getResource("/").toURI()),
                modelsRoot = root.resolve("model");

        StringProperties modelProperties = new StringProperties();
        modelProperties.setProperty(EmfModel.PROPERTY_NAME, "Model");
        modelProperties.setProperty(EmfModel.PROPERTY_FILE_BASED_METAMODEL_URI,
                modelsRoot.resolve("foo.emf").toAbsolutePath().toUri().toString()
        );
//        modelProperties.setProperty(EmfModel.PROPERTY_MODEL_URI,
//                modelsRoot.resolve("Tree.xmi").toAbsolutePath().toUri().toString()
//        );

        EolRunConfiguration runConfig = EolRunConfiguration.Builder()
                .withModel(new EmfModel(), modelProperties)
                .withScript(root.resolve("Demo.eol"))
                .withParameter("Thread", Thread.class)
                .build();

        runConfig.run();
        System.out.println(runConfig.getResult());
    }

}
