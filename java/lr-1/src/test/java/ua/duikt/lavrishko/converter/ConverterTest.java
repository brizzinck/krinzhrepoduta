package ua.duikt.lavrishko.converter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ConverterTest {

    /** Еталон порахований окремо точними дробами: verify/reference.py. */
    @ParameterizedTest(name = "{0} у {1}")
    @CsvSource({
            "CM2,M2,0.0001",
            "CM2,ARE,1e-06",
            "CM2,HECTARE,1e-08",
            "CM2,ACRE,2.4710538146716533e-08",
            "M2,CM2,10000.0",
            "M2,ARE,0.01",
            "M2,HECTARE,0.0001",
            "M2,ACRE,0.0002471053814671653",
            "ARE,CM2,1000000.0",
            "ARE,M2,100.0",
            "ARE,HECTARE,0.01",
            "ARE,ACRE,0.024710538146716535",
            "HECTARE,CM2,100000000.0",
            "HECTARE,M2,10000.0",
            "HECTARE,ARE,100.0",
            "HECTARE,ACRE,2.4710538146716536",
            "ACRE,CM2,40468564.224",
            "ACRE,M2,4046.8564224",
            "ACRE,ARE,40.468564224",
            "ACRE,HECTARE,0.40468564224",
    })
    void allDirectionsMatchReference(AreaUnit from, AreaUnit to, double expected) {
        assertEquals(expected, Converter.convert(1, from, to), expected * 1e-12);
    }

    @Test
    void knownValues() {
        assertEquals("100", Converter.format(Converter.convert(1, AreaUnit.HECTARE, AreaUnit.ARE)));
        assertEquals("10000", Converter.format(Converter.convert(1, AreaUnit.M2, AreaUnit.CM2)));
        assertEquals("0.01", Converter.format(Converter.convert(100, AreaUnit.CM2, AreaUnit.M2)));
        assertEquals("4046.8564224", Converter.format(Converter.convert(1, AreaUnit.ACRE, AreaUnit.M2)));
    }

    @Test
    void unitLookup() {
        assertEquals(AreaUnit.CM2, AreaUnit.byName("cm²"));
        assertEquals(AreaUnit.M2, AreaUnit.byName("M2"));
        assertEquals(AreaUnit.ACRE, AreaUnit.byNumber(5));
        assertNull(AreaUnit.byNumber(6));
        assertNull(AreaUnit.byName("xyz"));
    }
}
