package com.mycollege.schedule.feature.schedule.data.repository

import com.mycollege.schedule.core.network.Network
import com.mycollege.schedule.feature.schedule.data.models.DataClasses
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

class WebParser {

    companion object {

        private const val TIMEOUT: Int = 10000
        private var doc: Document? = null
        private var groups: HashMap<String, HashMap<String, ArrayList<DataClasses.Group>>> = HashMap()
        private const val FULL_URL = "https://imsit.ru/timetable/stud/raspisan.html"
        private var PATTERN_URL = "https://imsit.ru/timetable/stud/"

        fun loadData(progress: (Int) -> Unit): HashMap<String, HashMap<String, ArrayList<DataClasses.Group>>> {
            doc = Network.Companion.connect(FULL_URL, TIMEOUT)

            val table: Element = doc!!.select("table")[0]
            val rows = table.select("tr")

            // First row
            val columns: Elements = rows[0].select("td")

            // Get all of the courses
            for (column in columns) {
                groups[column.text()] = HashMap()
            }

            val sorted = groups.toSortedMap(Comparator.comparingInt {
                it.split(" ")[0].toInt()
            })

            // Get all of the groups
            val total = sorted.size + 3
            var current = 2
            progress((current * 100) / total)

            // Get all of the groups
            for (i in 0 until sorted.size) {
                val specialityArray = HashMap<String, ArrayList<DataClasses.Group>>()

                for (row in rows.drop(1)) {
                    val tableData = row.select("td")
                    val a = tableData[i].select("a").attr("href")

                    // if cell is not empty
                    if (!tableData[i].text().equals("") && !tableData[i].text().equals(" ")) {

                        // getting group schedule
                        val schedule = Network.Companion.connect("$PATTERN_URL$a", TIMEOUT)

                        val weeks = DataClasses.Weeks(null, null)

                        for (j in 1..2) {
                            val tables: Element = schedule.select("table")[if (j == 1) 0 else 1] //odd week and even week as index on the page
                            val tRows = tables.select("tr")
                            val counts = tRows[0]
                            val period = tRows[1]

                            val week: HashMap<Int, ArrayList<DataClasses.Lesson>> = HashMap()

                            // getting day lessons
                            for (day in tRows.drop(2)) {
                                val data = day.select("td")
                                val dayWeek = DataClasses.DayWeek.findByShort(data[0].text())
                                val lessonsArray: ArrayList<DataClasses.Lesson> = ArrayList()

                                var l = 0
                                for (cell in data) {

                                    val text = cell.text()
                                    if (text.length < 4) {
                                        l++
                                        continue
                                    }

                                    val count = counts.select("td")[l].text().split("-")[0].toInt()
                                    val time: String = period.select("td")[l].text()

                                    val regex = Regex("""^(пр\.|л\.|лаб\.)\s*(.*?)(?:\s*([\wА-ЯЁ][а-яё]+ [А-ЯЁ]\.[А-ЯЁ]\.))?\s*(с/зал|[\d-]+(?:[а-яё]?)?)?\s*$""")
                                    val match = regex.find(text)

                                    val type = match?.groups?.get(1)?.value // Type: пр., л., лаб.
                                    val finalType = when (type) {
                                        "пр." -> "Практика"
                                        "л." -> "Лекция"
                                        else -> "Лаборатория"
                                    }

                                    var name = match?.groups?.get(2)?.value // Name: Электротехника
                                    var teacher = match?.groups?.get(3)?.value // Teacher: Лисин Д.А.
                                    val location = match?.groups?.get(4)?.value // Location: 1-126

                                    // If teacher info isn't found, we need to check if it's part of the course name
                                    if (teacher == null && name != null) {
                                        // Look for teacher-like structure at the end of the name
                                        val teacherRegex = Regex("""([А-ЯЁ][а-яё]+ [А-ЯЁ]\.[А-ЯЁ]\.)$""")
                                        val teacherMatch = teacherRegex.find(name)

                                        if (teacherMatch != null) {
                                            teacher = teacherMatch.value
                                            name = name.removeSuffix(teacher)
                                        }
                                    }

                                    // If location is not explicitly provided, we try to split name further (if possible)
                                    if (location == null) {
                                        val nameParts = name?.split(" ")?.toMutableList()

                                        if (nameParts != null && nameParts.size >= 3) {
                                            teacher = (teacher ?: "") + " " + nameParts.takeLast(3).joinToString(" ")
                                            name = nameParts.dropLast(3).joinToString(" ")
                                        }
                                    }

                                    // Final formatting for teacher
                                    teacher = teacher?.trim()?.let {
                                        if (it.matches(Regex("^[А-ЯЁ][а-яё]+ [А-ЯЁ]\\.[А-ЯЁ]\\.$"))) {
                                            it
                                        } else { // if "Докторов С.Э.." replace last '.' with ""
                                            it.replace(Regex("\\.+$"), "")
                                        }
                                    }

                                    lessonsArray.add(
                                        DataClasses.Lesson(
                                            count,
                                            time,
                                            finalType,
                                            name,
                                            teacher,
                                            location
                                        )
                                    )
                                    l++
                                }

                                // sort by lesson order in a day
                                val sortedLessons = lessonsArray.sortedWith(Comparator.comparingInt {
                                    it.count
                                })

                                // adding a lesson to a day
                                if (dayWeek != null) {
                                    week[dayWeek.id] = sortedLessons.toMutableList() as ArrayList<DataClasses.Lesson>
                                }
                            }

                            // sort by day order in a week
                            val sortedWeek = week.toSortedMap().toMutableMap() as HashMap<Int, ArrayList<DataClasses.Lesson>>

                            // setting full schedule on 2 weeks
                            if (j == 1) weeks.weekOdd = sortedWeek else weeks.weekEven = sortedWeek
                        }

                        val speciality: String = if (tableData[i].text().contains("СПО")) "СПО"
                        else if (tableData[i].text().contains("Мг")) "Магистратура" else "Бакалавриат"

                        if (specialityArray[speciality] != null)
                            specialityArray[speciality]?.add(
                                DataClasses.Group(
                                    tableData[i].text(),
                                    a,
                                    weeks
                                )
                            )
                        else {
                            val groupArray = ArrayList<DataClasses.Group>()
                            groupArray.add(DataClasses.Group(tableData[i].text(), a, weeks))
                            specialityArray[speciality] = groupArray
                        }
                    }

                }
                sorted[sorted.keys.elementAt(i)] = specialityArray.toSortedMap(java.util.Comparator.comparingInt
                { it.length }).toMutableMap() as HashMap<String, ArrayList<DataClasses.Group>>

                current++
                progress((current * 100) / total)  // update the progress
            }

            // remove empty course
            val delete: ArrayList<String> = ArrayList()
            sorted.keys.forEach { key ->
                if (sorted[key]?.isEmpty() == true) delete.add(key)
            }
            for (del in delete) {
                sorted.remove(del)
            }

            groups = sorted.toMutableMap() as HashMap<String, HashMap<String, ArrayList<DataClasses.Group>>>

            return groups
        }

    }

}