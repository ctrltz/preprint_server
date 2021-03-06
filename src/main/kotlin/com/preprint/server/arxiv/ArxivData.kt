package com.preprint.server.arxiv

import com.preprint.server.data.Data
import com.preprint.server.ref.Reference

data class ArxivData(
    val identifier : String,
    var datestamp: String = "",
    override var id : String = "",
    var abstract : String = "",
    var creationDate : String = "",
    var title : String = "",
    var lastUpdateDate : String? = null,
    val authors : MutableList<Author> = mutableListOf(),
    var categories : MutableList<String> = mutableListOf(),
    var comments : String? = null,
    var reportNo : String? = null,
    var journalRef : String? = null,
    var mscClass : String? = null,
    var acmClass : String? = null,
    var doi : String? = null,
    var license : String? = null,
    override var refList : MutableList<Reference> = mutableListOf(),
    override var pdfUrl : String = ""
): Data {

    data class Author(val name : String, val affiliation : String? = null)
}
