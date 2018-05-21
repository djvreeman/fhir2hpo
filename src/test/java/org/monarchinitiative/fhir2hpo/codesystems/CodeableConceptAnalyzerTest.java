package org.monarchinitiative.fhir2hpo.codesystems;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.junit.Test;
import org.monarchinitiative.fhir2hpo.loinc.exception.ConflictingInternalCodesException;
import org.monarchinitiative.fhir2hpo.loinc.exception.UnmappedCodeableConceptException;
import org.monarchinitiative.fhir2hpo.util.FhirMockUtils;

public class CodeableConceptAnalyzerTest {
	
	@Test
	public void testOneCode() throws UnmappedCodeableConceptException, ConflictingInternalCodesException {
		List<Coding> codings = new ArrayList<>();
		codings.add(FhirMockUtils.mockCoding("http://hl7.org/fhir/v2/0078", "LL"));
		CodeableConcept codeableConcept = FhirMockUtils.mockCodeableConcept(codings);
		String internalCode = CodeableConceptAnalyzer.getInternalCodeForCodeableConcept(codeableConcept);
		assertEquals("Expected internal code 'L' for external code 'LL'", "L", internalCode);
	}

	@Test
	public void testOneDistinctCode() throws UnmappedCodeableConceptException, ConflictingInternalCodesException {
		List<Coding> codings = new ArrayList<>();
		codings.add(FhirMockUtils.mockCoding("http://hl7.org/fhir/v2/0078", "LL"));
		codings.add(FhirMockUtils.mockCoding("http://hl7.org/fhir/v2/0078", "L"));
		CodeableConcept codeableConcept = FhirMockUtils.mockCodeableConcept(codings);
		String internalCode = CodeableConceptAnalyzer.getInternalCodeForCodeableConcept(codeableConcept);
		assertEquals("Expected internal code 'L' for external codes 'LL' and 'L'", "L", internalCode);
	}

	@Test(expected = UnmappedCodeableConceptException.class)
	public void testUnmappedSystem() throws UnmappedCodeableConceptException, ConflictingInternalCodesException {
		List<Coding> codings = new ArrayList<>();
		codings.add(FhirMockUtils.mockCoding("Other System", "LL"));
		CodeableConcept codeableConcept = FhirMockUtils.mockCodeableConcept(codings);
		CodeableConceptAnalyzer.getInternalCodeForCodeableConcept(codeableConcept);
	}

	@Test(expected = UnmappedCodeableConceptException.class)
	public void testUnmappedCode() throws UnmappedCodeableConceptException, ConflictingInternalCodesException {
		List<Coding> codings = new ArrayList<>();
		codings.add(FhirMockUtils.mockCoding("http://hl7.org/fhir/v2/0078", "Other Code"));
		CodeableConcept codeableConcept = FhirMockUtils.mockCodeableConcept(codings);
		CodeableConceptAnalyzer.getInternalCodeForCodeableConcept(codeableConcept);
	}

	@Test(expected = ConflictingInternalCodesException.class)
	public void testConflictingCodes() throws UnmappedCodeableConceptException, ConflictingInternalCodesException {
		List<Coding> codings = new ArrayList<>();
		codings.add(FhirMockUtils.mockCoding("http://hl7.org/fhir/v2/0078", "LL"));
		codings.add(FhirMockUtils.mockCoding("http://hl7.org/fhir/v2/0078", "HH"));
		CodeableConcept codeableConcept = FhirMockUtils.mockCodeableConcept(codings);
		CodeableConceptAnalyzer.getInternalCodeForCodeableConcept(codeableConcept);
	}

}
