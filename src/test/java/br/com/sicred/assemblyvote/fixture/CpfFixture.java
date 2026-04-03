package br.com.sicred.assemblyvote.fixture;

import br.com.caelum.stella.validation.CPFValidator;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.apache.commons.lang3.RandomUtils.nextInt;

public final class CpfFixture {

    private static final List<String> cpfs = new ArrayList<>();

    static {
        cpfs.add("17587596585");
        cpfs.add("54921900590");
        cpfs.add("84617242082");
        cpfs.add("68205103658");
        cpfs.add("76541821046");
        cpfs.add("67086236098");
        cpfs.add("08855641387");
        cpfs.add("72273158190");
        cpfs.add("94621943430");
        cpfs.add("36561776275");
    }

    private CpfFixture() {}

    public static String cpfValid() {
        return cpfs.get(nextInt(0, cpfs.size()));
    }

    public static String cpfInvalid() {
        String cpf = null;

        while (isNull(cpf) || new CPFValidator().invalidMessagesFor(cpf).isEmpty()) {
            cpf = randomNumeric(11);
        }

        return cpf;
    }
}
