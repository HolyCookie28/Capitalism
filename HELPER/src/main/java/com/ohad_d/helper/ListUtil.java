package com.ohad_d.helper;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ListUtil {

    public static <T> T getItemById(List<T> list, String idFs, Function<T, String> idExtractor) {
        return list.stream()
                .filter(item -> idFs.equals(idExtractor.apply(item)))
                .findFirst()
                .orElse(null);
    }


    //    USAGE:
    //        City city = ListUtil.getItemById(cities, item.getCityId(), City::getIdFs);


    public static <T> List<T> addTopElement(
            List<T> list,
            Supplier<T> instanceSupplier,
            BiConsumer<T, String> idFsSetter,
            String idFsValue,
            BiConsumer<T, String> nameSetter,
            String nameValue) {

        // Create a new instance of T using the provided supplier
        T newInstance = instanceSupplier.get();

        // Set the properties using the provided setters
        idFsSetter.accept(newInstance, idFsValue);
        nameSetter.accept(newInstance, nameValue);

        // Add the new instance to the top of the list
        list.add(0, newInstance);

        return list;
    }

    //    USAGE:
    //       ListUtil.addTopElement(cities,
    //                              City::new,
    //                              City::setIdFs, "0",
    //                              City::setName, "Select a city");


}
