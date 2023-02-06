package fr.theogiraudet.test;

import fr.theogiraudet.test.a.A;
import fr.theogiraudet.test.a.AFactory;
import fr.theogiraudet.test.a.APackage;
import fr.theogiraudet.test.b.BPackage;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.epsilon.emc.emf.EmfModel;
import org.eclipse.epsilon.emc.emf.InMemoryEmfModel;
import org.eclipse.epsilon.etl.EtlModule;
import org.eclipse.epsilon.etl.strategy.DefaultTransformationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.Map;

public class Main {

    private static Logger logger = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) throws Exception {

        final A model = AFactory.eINSTANCE.createA();

        ResourceSet rs = new ResourceSetImpl();
        rs.getPackageRegistry().put(APackage.eNS_URI, APackage.eINSTANCE);
        rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("a", new EcoreResourceFactoryImpl());

        rs.getPackageRegistry().put(BPackage.eNS_URI, BPackage.eINSTANCE);
        rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("b", new EcoreResourceFactoryImpl());

        final Resource fooResource = createResource(model, "foo.a", rs);

        final EmfModel fooModel = createModel(fooResource, "foo.a");
        final EmfModel aMetaModel = createModel(APackage.eINSTANCE.eResource(), "A");
        final EmfModel bMetaModel = createModel(BPackage.eINSTANCE.eResource(), "B");


        EtlModule module = new EtlModule();
        module.getContext().setModule(module);
        module.parse(Main.class.getClassLoader().getResource("transformation/model_transformation.etl"));
        module.getContext().getModelRepository().addModel(aMetaModel);
        module.getContext().getModelRepository().addModel(bMetaModel);
        module.getContext().getModelRepository().addModel(fooModel);
        module.getContext().setTransformationStrategy(new DefaultTransformationStrategy());
        var result = module.execute();
        System.out.println();
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
