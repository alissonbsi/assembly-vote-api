package br.com.realizecfi.bffrpcustomeracquisition.fixture;

import lombok.SneakyThrows;
import org.instancio.Instancio;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.jeasy.random.api.Randomizer;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.ZoneId;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

/**
 * Classe utilitária usada para a geração de Fixtures (Objetos preenchidos com dados aleatórios).
 *
 * Exemplos de uso: Para retornar um objeto preenchido que tenha padrão Builder:
 * Response = Fixture.make(Response.builder().build());
 *
 * Para retornar um objeto preenchido que tenha padrão de Setter/Construtor vazio:
 * Response = Fixture.make(newResponse());
 *
 * Também é possível retornar um builder:
 *
 * Response.ResponseBuilder = Fixture.make(Response.builder());
 *
 * Este builder retorna com todos os atributos preenchidos, e é possível sobreescrever os atributos que quiser antes de
 * dar o .build();
 */
public class Fixture {

    private static final EasyRandom easyRandom;

    static {
        EasyRandomParameters parameters = new EasyRandomParameters()
            .randomize(XMLGregorianCalendar.class, new XMLGregorianCalendarRandomizer());
        easyRandom = new EasyRandom(parameters);
    }

    public static <T> T make(final Class<T> clazz) {
        return Instancio.create(clazz);
    }

    public static <T> List<T> makeList(final Class<T> clazz) {
        return makeList(clazz, 1);
    }

    public static <T> List<T> makeList(final Class<T> clazz, final Integer size) {
        return Instancio.ofList(clazz)
            .size(size)
            .create();
    }

    public static <T> T make(final T mockClass){
        return (T) easyRandom.nextObject(mockClass.getClass());
    }

    public static class XMLGregorianCalendarRandomizer implements Randomizer<XMLGregorianCalendar> {

        @Override
        @SneakyThrows
        public XMLGregorianCalendar getRandomValue() {
            // This will generate a dateTime value with this pattern: 2021-08-06T12:59:39.151
            return DatatypeFactory.newInstance()
                .newXMLGregorianCalendar(
                    (GregorianCalendar) GregorianCalendar.getInstance(TimeZone.getTimeZone(ZoneId.systemDefault()))
                );

        }
    }
}
