package com.mycollege.schedule.app.tests

import org.junit.jupiter.api.Test

class ParsingTest {

    private fun parsing(text: String) {
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

        // Print the results
        println("""
        |$finalType
        |$name 
        |$teacher
        |$location""")
    }


    @Test
    fun test() {
        parsing("пр.Физическая культура с/зал")
        parsing("л.Кубановедение Клечковская Е.В. 2-404")
        parsing("пр.Профессиональная этика и психология делового общения Оздоган И.С. 2-409")
        parsing("пр.Экономический анализ Крутова А.В. 1-236")
        parsing("пр.Информационные технологии в профессиональной деятельности Докторов С.Э. 1-114а")
        parsing("л.Информационные технологии в профессиональной деятельности Докторов С.Э. 1-308")
        parsing("л.Современные педагогические технологии Салменкова М.В. .")
        parsing("пр.Выполнение работ по профессии рабочего: 16437 - Парикмахер Рязанова М.Е. 1-127")
    }

}