package org.monarchinitiative.fhir2hpo.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.fhir2hpo.hpo.HpoTermWithNegation;
import org.monarchinitiative.fhir2hpo.loinc.Loinc2HpoAnnotation;
import org.monarchinitiative.fhir2hpo.loinc.LoincId;
import org.monarchinitiative.fhir2hpo.loinc.LoincScale;
import org.monarchinitiative.fhir2hpo.loinc.exception.MalformedLoincCodeException;
import org.monarchinitiative.phenol.ontology.data.ImmutableTermId;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermId;

public class LoincAnnotationParser {
	private static final Logger logger = LogManager.getLogger();

	public static Map<LoincId, Loinc2HpoAnnotation> parse(File file, Map<TermId, Term> hpoTermMap) throws FileNotFoundException {

		Map<LoincId, Loinc2HpoAnnotation.Builder> builders = new LinkedHashMap<>();
		BufferedReader reader = new BufferedReader(new FileReader(file));
		reader.lines().forEach(serialized -> {
			String[] elements = serialized.split("\\t");
			if (elements.length == 13 && !serialized.startsWith("loincId")) {
				try {
					// Only process if finalized
					boolean finalized = Boolean.parseBoolean(elements[11]);
					if (finalized) {
						LoincId loincId = new LoincId(elements[0]);
						LoincScale loincScale = LoincScale.string2enum(elements[1]);
						String code = elements[3];
						TermId termId = ImmutableTermId.constructWithPrefix(elements[4]);
						boolean isNegated = Boolean.parseBoolean(elements[5]);

						if (!builders.containsKey(loincId)) {
							builders.put(loincId, new Loinc2HpoAnnotation.Builder());
							builders.get(loincId).setLoincId(loincId).setLoincScale(loincScale);
						}

						Term term = hpoTermMap.get(termId);
						if (term == null) {
							// This should not be an issue in the long run, but
							// for now there may be disconnects in what terms
							// are annotated versus
							// what terms are released in the hpo.
							logger.error("The HPO Term could not be found for Term Id " + termId);
						} else {
							HpoTermWithNegation termWithNegation = new HpoTermWithNegation(term, isNegated);
							builders.get(loincId).addMapping(code, termWithNegation);
						}
					}
				} catch (MalformedLoincCodeException e) {
					logger.error("Malformed loinc code line: " + serialized);
				}
			} else {
				if (elements.length != 13) {
					logger.error(String.format("line does not have 13 elements, but has %d elements. Line: %s",
							elements.length, serialized));
				} else {
					logger.debug("line is header: " + serialized);
				}

			}
		});

		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Map<LoincId, Loinc2HpoAnnotation> annotationMap = new LinkedHashMap<>();
		builders.entrySet().forEach(p -> annotationMap.put(p.getKey(), p.getValue().build()));
		return annotationMap;
	}
	

}
