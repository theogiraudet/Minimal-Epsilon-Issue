package net.gunivers.dispenser.observer.serializer;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import net.gunivers.dispenser.observer.model.ModelElement;

import java.util.Arrays;

public class ModelElementBeanSerializerModifier extends BeanSerializerModifier {

    @Override
    public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc, JsonSerializer<?> serializer) {
        if (Arrays.asList(beanDesc.getBeanClass().getInterfaces()).contains(ModelElement.class)) {
            return new ModelElementSerializer((JsonSerializer<Object>) serializer);
        }

        return serializer;
    }

}
