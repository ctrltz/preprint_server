package com.preprint.server.ref

import pl.edu.icm.cermine.ContentExtractor
import java.io.ByteArrayInputStream
import java.io.InputStream

object CermineReferenceExtractor : ReferenceExtractor {
    override fun extract(pdf : ByteArray): List<Reference> {
        val extractor = ContentExtractor()
        val inputStream: InputStream = ByteArrayInputStream(pdf)
        extractor.setPDF(inputStream)
        val references = extractor.references
        return Reference.toReferences(references.map { it.text })
    }
}