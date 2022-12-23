package net.gunivers.dispenser.observer.transformer;

public interface Transformer<T, U> {

    U transform(T source);

}
