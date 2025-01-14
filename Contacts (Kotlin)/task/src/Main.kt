package contacts

import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.system.exitProcess

const val RECORD_ADDED_MESSAGE = "The record added."
const val NO_RECORDS_TO_EDIT_MESSAGE = "No records to edit!"
const val ENTER_MAIN_MENU_ACTION_MESSAGE = "[menu] Enter action (add, list, search, count, exit):"
private const val SELECT_A_RECORD_MESSAGE = "Select a record:"
private const val YOU_MUST_ENTER_NUMBER_MESSAGE = "You must enter a number!"
private const val INVALID_RECORD_NUMBER = "Invalid record."
private const val SAVED_MESSAGE = "Saved"
private const val INVALID_RECORD_FIELD_MESSAGE = "Invalid record field!"
private const val RECORD_REMOVED_MESSAGE = "The record removed!"
private const val WRONG_NUMBER_FORMAT_MESSAGE = "Wrong number format!"
private const val PHONE_BOOK_COUNT_MESSAGE = "The Phone Book has %d records."
private const val ENTER_NUMBER_MESSAGE = "Enter the number:"
private const val ENTER_SURNAME_MESSAGE = "Enter the surname:"
private const val ENTER_NAME_MESSAGE = "Enter the name:"
private const val ENTER_BIRTH_DATE_MESSAGE = "Enter the birth date:"
private const val BAD_BIRTH_DATE_MESSAGE = "Bad birth date!"
private const val ENTER_THE_GENDER_MESSAGE = "Enter the gender (M, F):"
private const val BAD_GENDER_MESSAGE = "Bad gender!"
private const val ENTER_CONTACT_TYPE_MESSAGE = "Enter the type (person, organization):"
private const val ENTER_ADDRESS_MESSAGE = "Enter the address:"
private const val ENTER_ORGANIZATION_NAME_MESSAGE = "Enter the organization name:"
private const val ENTER_INDEX_FOR_INFO_MESSAGE = "Enter index to show info:"
const val NO_NUMBER_TEXT = "[no number]"

var contacts = mutableListOf<Contact>()

var dataFile: File? = null

fun main(args: Array<out String>) {

    if (args.isNotEmpty()) {
        dataFile = File(args[0])
        if (!dataFile?.exists()!!) {
            dataFile?.createNewFile()
        } else {
            val inputStream = ObjectInputStream(dataFile?.inputStream())
            contacts = inputStream.readObject() as MutableList<Contact>
        }
    }

    while (true) {
        println(ENTER_MAIN_MENU_ACTION_MESSAGE)

        val input = readln()

        when (input.lowercase()) {
            "add" -> {
                val contact = collectContactInfo() ?: continue
                contacts.add(contact)
            }

            "edit" -> {
                if (contacts.isEmpty()) {
                    println(NO_RECORDS_TO_EDIT_MESSAGE)
                    continue
                }

                listContacts()

                println(SELECT_A_RECORD_MESSAGE)

                var recordInput: Int?
                while (true) {
                    recordInput = readln().toIntOrNull()

                    if (recordInput != null) break
                    println(YOU_MUST_ENTER_NUMBER_MESSAGE)
                }

                val recordIndex = recordInput!! - 1

                if (0 > recordIndex || recordIndex >= contacts.size) {
                    println(INVALID_RECORD_NUMBER)
                    continue
                }

                editContact(contacts[recordIndex])

                println(SAVED_MESSAGE)
            }

            "count" -> {
                println(PHONE_BOOK_COUNT_MESSAGE.format(contacts.size))
            }

            "list" -> {
                listContacts()

                println(ENTER_INDEX_FOR_INFO_MESSAGE)
                val index = readln().toInt() - 1
                println(contacts[index].getInfo())
                println()

                while (true) {
                    println("[record] Enter action (edit, delete, menu):")

                    when (readln().lowercase()) {
                        "menu" -> {
                            break
                        }

                        "delete" -> {
                            contacts.removeAt(index)
                            println(RECORD_REMOVED_MESSAGE)
                        }

                        "edit" -> {
                            editContact(contacts[index])
                        }
                    }

                    println()
                }
            }

            "search" -> {
                var action: String
                var results = performSearch()
                println()

                while (true) {
                    println("[search] Enter action ([number], back, again):")
                    action = readln().lowercase()

                    when {

                        action.toIntOrNull() != null -> {
                            val index = action.toInt() - 1
                            println(results[index].getInfo())
                            println()

                            println("[record] Enter action (edit, delete, menu):")
                            action = readln().lowercase()

                            when (action) {
                                "menu" -> {
                                    break
                                }

                                "delete" -> {
                                    contacts.removeAt(index)
                                    println(RECORD_REMOVED_MESSAGE)
                                }

                                "edit" -> {
                                    editContact(results[index])
                                }
                            }
                        }

                        action.equals("back", true) -> {
                            break
                        }

                        action.equals("again", true) -> {
                            results = performSearch()
                        }
                    }

                    println()
                }
            }

            "exit" -> {
                exitProcess(0)
            }
        }

        println()
    }
}


private fun saveContacts() {
    if (dataFile?.exists() ?: return) {
        ObjectOutputStream(dataFile?.outputStream()).writeObject(contacts)
        println(SAVED_MESSAGE)
    }
}

private fun editContact(contact: Contact) {
    println("Select a field (${contact.properties.joinToString()}):")

    when (readln().lowercase()) {
        "name" -> {
            println(ENTER_NAME_MESSAGE)
            contact.name = readln()
            saveContacts()
        }

        "surname" -> {
            println(ENTER_SURNAME_MESSAGE)
            (contact as PersonalContact).surname = readln()
            saveContacts()
        }

        "birthdate" -> {
            println("Enter birthdate:")
            var birthdate = readln()

            if (!checkBirthdateFormat(birthdate)) {
                println(BAD_BIRTH_DATE_MESSAGE)
                birthdate = ""
            }

            (contact as PersonalContact).birthdate = birthdate
            saveContacts()
        }

        "gender" -> {
            println("Enter gender:")
            var gender = readln()

            if (!checkGenderFormat(gender)) {
                println(BAD_GENDER_MESSAGE)
                gender = ""
            }

            (contact as PersonalContact).gender = gender
            saveContacts()
        }

        "number" -> {
            println(ENTER_NUMBER_MESSAGE)
            var number = readln()
            if (!checkNumberFormat(number)) {
                println(WRONG_NUMBER_FORMAT_MESSAGE)
                number = ""
            }

            contact.number = number
            saveContacts()
        }

        "address" -> {
            println("Enter address:")
            val address = readln()

            (contact as BusinessContact).address = address
            saveContacts()
        }

        else -> {
            println(INVALID_RECORD_FIELD_MESSAGE)
        }
    }

    println(contact.getInfo())
}


private fun performSearch(): List<Contact> {

    println("Enter search query:")
    val query = readln()

    val results = contacts.filter { Regex(query, RegexOption.IGNORE_CASE).containsMatchIn(it.propertyValues) }
    println("Found ${results.size} results:")
    results.forEachIndexed { index, contact -> println("${index + 1}. ${contact.name}${if (contact is PersonalContact) " " + contact.surname else ""}") }

    return results
}

private fun collectContactInfo(): Contact? {
    var name = ""
    var surname = ""
    var birthdate = ""
    var gender = ""
    var address = ""
    var number = ""

    println(ENTER_CONTACT_TYPE_MESSAGE)
    val type = readln().lowercase()
    when (type) {
        "person" -> {
            println(ENTER_NAME_MESSAGE)
            name = readln()

            println(ENTER_SURNAME_MESSAGE)
            surname = readln()

            println(ENTER_BIRTH_DATE_MESSAGE)
            birthdate = readln()

            if (!checkBirthdateFormat(birthdate)) {
                println(BAD_BIRTH_DATE_MESSAGE)
            }

            println(ENTER_THE_GENDER_MESSAGE)
            gender = readln()
            if (!checkGenderFormat(gender)) {
                println(BAD_GENDER_MESSAGE)
            }
        }

        "organization" -> {
            println(ENTER_ORGANIZATION_NAME_MESSAGE)
            name = readln()

            println(ENTER_ADDRESS_MESSAGE)
            address = readln()
        }

        else -> {
            println("Invalid contact type!")
            return null
        }
    }

    number = requestPhoneNumber()

    println(RECORD_ADDED_MESSAGE)

    return if (type == "person") {
        PersonalContact(name, surname, birthdate, gender, number)
    } else {
        BusinessContact(name, address, number)
    }
}

fun listContacts() {
    contacts.forEachIndexed { index, contact ->
        println("${index + 1}. ${contact.name}${if (contact is PersonalContact) " " + contact.surname else ""}")
    }
}

private fun requestPhoneNumber(): String {
    println(ENTER_NUMBER_MESSAGE)
    var number = readln()

    if (!checkNumberFormat(number)) {
        number = ""
        println("Wrong number format!")
    }

    return number
}

private fun checkNumberFormat(value: String): Boolean {/* Split the number into groups that will each be matched against a regex individually. */
    val groups = value.split('-', ' ').toMutableList()

    if (groups.size > 1) {
        var surroundedCount = 0
        for (group in groups) {
            if (group.hasSurrounding('(', ')')) surroundedCount++

            if (surroundedCount > 1) return false
        }

        if (groups[0].length > 2 && groups[0].contains("+")) return false

        var index = 1
        while (index < groups.size) {/* Remove parenthesis from group to make regex pattern matching easier. */
            groups[index] = groups[index].removeSurrounding("(", ")")

            if (!groups[index].matches(Regex("[^\\W_]{2,}"))) return false
            index++
        }
    } else {
        val group = groups[0].replaceFirst("+", "").removeSurrounding("(", ")")
        if (!group.matches(Regex("[^\\W_]+"))) return false
    }

    return true
}


private fun checkBirthdateFormat(value: String): Boolean {
    return value.matches(Regex("\\d{2}/\\d{2}/\\d{4}"))
}

private fun checkGenderFormat(value: String): Boolean {
    return value.matches(Regex("[MmFf]"))
}

private fun String.hasSurrounding(prefix: Char, suffix: Char): Boolean {
    return this.startsWith(prefix) && this.endsWith(suffix)
}

abstract class Contact(var name: String, var number: String = "") {
    val timeCreated: LocalDateTime
    var timeLastEdit: LocalDateTime

    abstract val properties: List<String>

    abstract val propertyValues: String

    init {
        val time = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)
        timeCreated = time
        timeLastEdit = time
    }

    abstract fun getInfo(): String

    fun hasNumber(): Boolean {
        return !(number.isBlank() || number.isEmpty())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Contact

        if (name != other.name) return false
        return number == other.number
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + number.hashCode()
        return result
    }

    override fun toString(): String {
        return "Contact(name='$name', number='$number')"
    }
}

class PersonalContact(name: String, var surname: String, var birthdate: String, var gender: String, number: String) :
    Contact(name, number) {
    override val properties: List<String>
        get() = listOf("name", "surname", "birthdate", "gender", "number")
    override val propertyValues: String
        get() = "$name$surname$birthdate$gender$number"

    override fun getInfo(): String {
        val birthdateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return """
            Name: $name
            Surname: $surname
            Birth date: ${if (hasBirthdate()) birthdate.format(birthdateFormatter) else "[no data]"}
            Gender: ${if (hasGender()) gender else "[no data]"}
            Number: ${if (hasNumber()) number else NO_NUMBER_TEXT}
            Time created: ${timeCreated.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}
            Time last edit: ${timeLastEdit.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}
        """.trimIndent()
    }

    private fun hasGender(): Boolean {
        return gender.isNotBlank() && gender.isNotEmpty()
    }

    private fun hasBirthdate(): Boolean {
        return birthdate.isNotBlank() && birthdate.isNotEmpty()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as PersonalContact

        if (surname != other.surname) return false
        if (birthdate != other.birthdate) return false
        return gender == other.gender
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + surname.hashCode()
        result = 31 * result + birthdate.hashCode()
        result = 31 * result + gender.hashCode()
        return result
    }

    override fun toString(): String {
        return "PersonContact(surname='$surname', birthDate=$birthdate, gender='$gender')"
    }
}

class BusinessContact(name: String, var address: String, number: String) : Contact(name, number) {
    override val properties: List<String>
        get() =  listOf("name", "address", "number")
    override val propertyValues: String
        get() = "$name$address$number"

    override fun getInfo(): String {
        return """
            Organization name: $name
            Address: $address
            Number: ${if (hasNumber()) number else NO_NUMBER_TEXT}
            Time created: ${timeCreated.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}
            Time last edit: ${timeLastEdit.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}
        """.trimIndent()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as BusinessContact

        return address == other.address
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + address.hashCode()
        return result
    }

    override fun toString(): String {
        return "CompanyContact(address='$address')"
    }
}