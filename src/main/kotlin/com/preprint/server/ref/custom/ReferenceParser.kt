package com.preprint.server.ref.custom

import org.apache.logging.log4j.kotlin.logger

object ReferenceParser {
    val logger = logger()
    fun parse(
        lines: List<Line>,
        refType : ReferenceType,
        isTwoColumns: Boolean,
        pageWidth: Int
    ) : List<String> {
        logger.info("Begin reference parsing")
        val refList = mutableListOf<String>()
        val refRegex = refType.regex
        if (!isTwoColumns) {
            //analyze this text
            var canUseSecondIndentPattern = true
            var i = 0
            var secondLineIndent = -1
            var curRefNum = 1
            val firstLineIndices = mutableListOf<Int>()
            var maxWidth = 0
            while (i < lines.size) {
                firstLineIndices.add(i)
                //find next reference
                maxWidth = Integer.max(maxWidth, lines[i].lastPos - lines[i].indent)
                var j = i + 1
                while (j < lines.size) {
                    val match = refRegex.find(lines[j].str)
                    if (match != null) {
                        if (refType.isNum) {
                            val value = match.value.drop(refType.firstLen).dropLast(refType.lastLen).toInt()
                            if (value == curRefNum + 1) {
                                if (refType.strict && lines[j].indent == secondLineIndent) {
                                    logger.info("Drop because first line indent is equal to second line indent")
                                    return listOf()
                                }
                                else {
                                    break
                                }
                            }
                            else if (value == curRefNum) {
                                return listOf()
                            }
                        }
                        else if (refType.strict && lines[j].indent == secondLineIndent) {
                            logger.info("Drop because first line indent is equal to second line indent")
                            return listOf()
                        } else {
                            break
                        }
                    }
                    j += 1
                }

                if (j != lines.size) {
                    for (k in i + 1 until j) {
                        if (secondLineIndent == -1) {
                            secondLineIndent = lines[k].indent
                        }
                        if (secondLineIndent != lines[k].indent) {
                            canUseSecondIndentPattern = false
                        }
                    }
                } else {
                    //this was the last reference
                    break
                }
                curRefNum += 1
                i = j
            }

            if (refType.strict && !canUseSecondIndentPattern) {
                logger.info("Drop because can't use indent pattern")
                return listOf()
            }

            logger.info("Found $curRefNum references")

            //parse references
            for ((j, lineInd) in firstLineIndices.withIndex()) {
                var curRef = ""
                if (j != firstLineIndices.lastIndex) {
                    val nextLineInd = firstLineIndices[j + 1]
                    for (k in lineInd until nextLineInd) {
                        val newLine = if (k == lineInd) removeRefPattern(lines[k].str, refRegex) else lines[k].str
                        if (newLine == null) {
                            return listOf()
                        }
                        curRef = addLineToReference(curRef, newLine)

                        if ((curRef.last() == '.' || nextLineInd - lineInd > 5)
                            && ((k != lineInd && lines[k].lastPos < lines[k - 1].lastPos * 0.9)
                                    || k == lineInd && (lines[k].lastPos - lines[k].indent) < maxWidth * 0.9)
                        ) {

                            //then this is the end of reference
                            break
                        }
                    }
                } else {
                    var linesParsed = 0
                    //this is the last reference and we should find it's end
                    for (k in lineInd until lines.size) {
                        val newLine = if (k == lineInd) removeRefPattern(lines[k].str, refRegex) else lines[k].str
                        if (newLine == null) {
                            return listOf()
                        }
                        linesParsed += 1
                        if (linesParsed > 5) {
                            return listOf()
                        }
                        curRef = addLineToReference(curRef, newLine)
                        if (canUseSecondIndentPattern && k < lines.lastIndex &&
                            lines[k + 1].indent != secondLineIndent
                        ) {
                            //we find the end of reference
                            break
                        }
                        if (k > lineInd) {
                            if (lines[k].lastPos < lines[k - 1].lastPos * 0.9) {
                                //we find the end of reference
                                break
                            }
                        } else {
                            if (lines[k].lastPos - lines[k].indent < 0.9 * maxWidth) {
                                //we find the end of reference
                                break
                            }
                        }
                    }
                }
                refList.addAll(curRef.split(";"))
            }
        } else {
            var canUseSecondIndentPattern = true
            var i = 0
            var secondLineIndentLeft = -1
            var secondLineIndentRight = -1

            //current page side(0 -- left, 1 -- right)
            fun getSide(line: Line): Int {
                return if (line.indent < pageWidth * 0.4) 0 else 1
            }

            var curRefNum = 1
            val firstLineIndices = mutableListOf<Int>()
            var maxWidth = 0
            //analyze this text
            while (i < lines.size) {
                firstLineIndices.add(i)
                //find next reference
                maxWidth = Integer.max(maxWidth, lines[i].lastPos - lines[i].indent)
                var j = i + 1
                while (j < lines.size) {
                    val match = refRegex.find(lines[j].str)
                    val secondLineIndent = when {
                        getSide(lines[j]) == 0 -> secondLineIndentLeft
                        else                   -> secondLineIndentRight
                    }
                    if (match != null) {
                        if (refType.isNum) {
                            val value = match.value.drop(refType.firstLen).dropLast(refType.lastLen).toInt()
                            if (value == curRefNum + 1) {
                                if (!refType.strict || lines[j].indent != secondLineIndent) {
                                    break
                                }
                                else {
                                    logger.info("Drop because first line indent is equal to second line indent")
                                    return listOf()
                                }
                            }
                            else if (value == curRefNum) {
                                return listOf()
                            }
                        }
                        else if (refType.strict && lines[j].indent == secondLineIndent) {
                            logger.info("Drop because first line indent is equal to second line indent")
                            return listOf()
                        } else {
                            break
                        }
                    }
                    j += 1
                }
                if (j != lines.size) {
                    for (k in i + 1 until j) {
                        val curSide = getSide(lines[k])
                        if (curSide == 0) {
                            if (secondLineIndentLeft == -1) {
                                secondLineIndentLeft = lines[k].indent
                            }
                            if (secondLineIndentLeft != lines[k].indent) {
                                canUseSecondIndentPattern = false
                            }
                        } else {
                            if (secondLineIndentRight == -1) {
                                secondLineIndentRight = lines[k].indent
                            }
                            if (secondLineIndentRight != lines[k].indent) {
                                canUseSecondIndentPattern = false
                            }
                        }
                    }
                } else {
                    //this was the last reference
                    break
                }
                curRefNum += 1
                i = j
            }

            if (refType.strict && !canUseSecondIndentPattern) {
                logger.info("Drop because can't use indent pattern")
                return listOf()
            }

            logger.info("Found $curRefNum reference lines")

            //parse references
            for ((j, lineInd) in firstLineIndices.withIndex()) {
                var curRef = ""
                if (j != firstLineIndices.lastIndex) {
                    val nextLineInd = firstLineIndices[j + 1]
                    var prevSide = 0
                    for (k in lineInd until nextLineInd) {
                        val newLine = if (k == lineInd) removeRefPattern(lines[k].str, refRegex) else lines[k].str
                        if (newLine == null) {
                            return listOf()
                        }
                        curRef = addLineToReference(curRef, newLine)
                        val curSide = getSide(lines[k])
                        if ((curRef.last() == '.' || nextLineInd - lineInd > 10)
                            && ((k != lineInd && curSide == prevSide && lines[k].lastPos < lines[k - 1].lastPos * 0.9)
                                    || (lines[k].lastPos - lines[k].indent) < maxWidth * 0.8)
                        ) {

                            //then this is the end of reference
                            break
                        }
                        prevSide = curSide
                    }
                } else {
                    //this is the last reference and we should find it's end
                    var prevSide = 0
                    var linesParsed = 0
                    for (k in lineInd until lines.size) {
                        val newLine = if (k == lineInd) removeRefPattern(lines[k].str, refRegex) else lines[k].str
                        if (newLine == null) {
                            return listOf()
                        }
                        linesParsed += 1
                        if (linesParsed > 9) {
                            return listOf()
                        }
                        curRef = addLineToReference(curRef, newLine)
                        val curSide = getSide(lines[k])
                        if (canUseSecondIndentPattern && k < lines.lastIndex) {
                            if (curSide == 0 && lines[k + 1].indent != secondLineIndentLeft
                                || curSide == 1 && lines[k + 1].indent != secondLineIndentRight
                            ) {

                                //we find the end of reference
                                break
                            }
                        }
                        if (k > lineInd) {
                            if (prevSide == curSide && lines[k].lastPos < lines[k - 1].lastPos * 0.9) {
                                //we find the end of reference
                                break
                            }
                        } else {
                            if (lines[k].lastPos - lines[k].indent < 0.8 * maxWidth) {
                                //we find the end of reference
                                break
                            }
                        }
                        prevSide = curSide
                    }
                }
                refList.addAll(curRef.split(";").map{it.trimIndent()})
            }
        }
        logger.debug(refList.mapIndexed {i, ref -> "  ${i + 1}) $ref"}.joinToString(prefix = "\n", separator = "\n"))
        return refList
    }

    private fun addLineToReference(ref: String, line: String): String {
        return if (ref.length > 2 && ref.last() == '-') {
            if (ref[ref.lastIndex - 1].isLowerCase() && line.first().isLowerCase()) {
                ref.dropLast(1) + line
            } else {
                ref + line
            }
        } else {
            if (ref == "") {
                line
            } else {
                ref + " " + line
            }
        }
    }

    private fun removeRefPattern(ref : String, regex : Regex) : String? {
        val match = regex.find(ref) ?: return null
        return ref.drop(match.range.last + 1).trimIndent()
    }
}