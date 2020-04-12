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

    override fun toString(): String {
        var res = ""
        res += "id: $id\n"

        res += "Creation date: $creationDate\n"

        if (lastUpdateDate != null) {
            res += "Update date: $lastUpdateDate\n"
        }

        res += "Title: $title\n"

        res += "Authors:\n"
        res += authors.foldIndexed("") { i, acc, author -> "$acc  ${i + 1}) ${author.name}\n" }

        res += "Categories:\n"
        res += categories.foldIndexed("") { i, acc, category -> "$acc  ${i + 1}) ${category}\n" }
        if (comments != null) {
            res += "Comments:\n$comments\n"
        }
        if (reportNo != null) {
            res += "Report number: $reportNo\n"
        }
        if (journalRef != null) {
            res += "Journal reference: $journalRef\n"
        }
        if (mscClass != null) {
            res += "MSC class: $mscClass\n"
        }
        if (acmClass != null) {
            res += "ACM class: $acmClass\n"
        }
        if (doi != null) {
            res += "DOI: $doi\n"
        }
        res += "License: $license\n"

        res += "PDF url: $pdfUrl\n"

        res += "References:\n"
        res += refList.foldIndexed("") { i, acc, reference -> "$acc  ${i + 1}) ${reference}\n" }

        return res + "\n\n"
    }
}
