package me.damon.schoolbot.objects.school

class School(
    var name: String,
    var url: String,
    var emailSuffix: String,
    var isPittSchool: Boolean,
    var guildId: Long = -1L,
    var roleId: Long = -1L,
    var id: Long = -1L,

    var classroomList: ArrayList<Classroom> = arrayListOf(),
    var professorList: ArrayList<Professor> = arrayListOf()
)
{


    fun addProfessor(professor: Professor)
    {
        professorList.add(professor)
    }



    fun getProfessorById(id: Int): Professor?
    {
        return professorList
            .stream()
            .filter { it.id == id }
            .findFirst()
            .orElse(null)
    }

    fun getClassroomById(id: Int): Classroom?
    {
        return classroomList
            .stream()
            .filter { it.id == id }
            .findFirst()
            .orElse(null)
    }

    fun hasProfessors(): Boolean
    {
        return professorList.isNotEmpty()
    }

    fun hasClassrooms(): Boolean
    {
        return classroomList.isNotEmpty()
    }

}