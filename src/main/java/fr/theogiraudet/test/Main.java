package fr.theogiraudet.test;

import fr.theogiraudet.test.a.A;
import fr.theogiraudet.test.a.AFactory;
import fr.theogiraudet.test.a.APackage;
import fr.theogiraudet.test.b.BPackage;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.epsilon.emc.emf.EmfModel;
import org.eclipse.epsilon.emc.emf.InMemoryEmfModel;
import org.eclipse.epsilon.etl.EtlModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.Map;

public class Main {

    private static Logger logger = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) throws Exception {

        EPackage.Registry.INSTANCE.put(APackage.eNS_URI, APackage.eINSTANCE);
        EPackage.Registry.INSTANCE.put(BPackage.eNS_URI, BPackage.eINSTANCE);
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("a", new XMIResourceFactoryImpl());
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("b", new XMIResourceFactoryImpl());

        A instance = AFactory.eINSTANCE.createA();
        instance.setName("foo2");

        ResourceSet resourceSet = new ResourceSetImpl();
        resourceSet.getPackageRegistry().put(APackage.eNS_URI, APackage.eINSTANCE);
        Resource resource = resourceSet.createResource(URI.createFileURI("foo.a"));
        resource.getContents().add(instance);
        InMemoryEmfModel sourceModel = new InMemoryEmfModel("A", resource);

        // The target model of the transformation
        EmfModel targetModel = new EmfModel();
        targetModel.setName("B");
        targetModel.setReadOnLoad(false); // As the model doesn't exist
        targetModel.setStoredOnDisposal(false); // We don't want to store the target model
        targetModel.setModelFileUri(URI.createFileURI("foo.b"));
        targetModel.setMetamodelUri(BPackage.eNS_URI);
        targetModel.load();

        EtlModule module = new EtlModule();
        module.getContext().setModule(module);
        module.parse(Main.class.getClassLoader().getResource("transformation/model_transformation.etl"));
        module.getContext().getModelRepository().addModel(sourceModel);
        module.getContext().getModelRepository().addModel(targetModel);

        // Execute the transformation
        module.execute();

        // Print all instances of B in the target model
        System.out.println(targetModel.getAllOfKind("B"));
    }

    public static Resource createResource(EObject model, String name, ResourceSet set) throws IOException {
        var uri = URI.createURI("https:///www.theogiraudet.fr/test/schemas/" + name);
        var resource = set.createResource(uri);
        resource.getContents().add(model);
        resource.load(Map.of());
        return resource;
    }

    public static EmfModel createModel(Resource resource, String name) {
        final InMemoryEmfModel model = new InMemoryEmfModel(resource);
        model.setName(name);
        return model;
    }

}
