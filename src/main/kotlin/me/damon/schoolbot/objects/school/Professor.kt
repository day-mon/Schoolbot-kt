package me.damon.schoolbot.objects.school

class Professor(
    var id: Int,
    var firstName: String,
    var lastName: String,
    var emailPrefix: String,
    var classes: ArrayList<Classroom> = arrayListOf()
)
{
    fun addClass(classroom: Classroom)
    {
        classes.add(classroom)
    }

    fun getFullName(): String
    {
        return "$firstName $lastName"
    }


}