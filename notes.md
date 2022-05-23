# Notes for things I have learned this project


**Kotlin Covariance & Contravariance** | [Picture with explanation](https://i.imgur.com/oKD9hPL.png)
> **Covariance** is the ability to take a type parameter and make it more specific.\
> **Accepts subtypes**
> 
> For example, if you have a function that takes a List<String> and returns a List<Any>, you can make the function return a List<Int> by changing the type parameter to Int. \
> **This is called covariance.**
> 
> Here is a java example:
>```java
> public class Covariance {
>     public static void main(String[] args) {
>       List<Mango> mangoes = new ArrayList<>();
>       List<Fruit> fruitList = new ArrayList<>();
>
>       mangoes = fruitList; // not allowed
>       fruitList = mangoes; // allowed
>    }
>}
>```
> To simply put it Covariance is **READ-ONLY**. Can be denoted in kotlin by `List<in T>`.
>

>
> **Contravariance** is the ability to take a type parameter and make it less specific.\
> **Accepts supertypes**
> 
> Here is a java example:
> ```java
> public class Contravariance {
>    public static void main(String[] args) {
>        Wrapper<Mango> mangoInfo = getFruitInfo();
>        Wrapper<Fruit> fruitInfo = getMangoInfo();
>       
>        fruitInfo = mangoInfo; // not allowed
>        mangoInfo = fruitInfo; // allowed
>   }
>    static void getFruitInfo(Fruit fruits) { }
>    static void getMangoInfo(Mango mango) { }
> }
> ```
> Simply to put it Contravariance is **WRITE-ONLY**. Can be denoted in kotlin by `List<out T>`.



**What is a coroutine?**
> A coroutine is a function that can be suspended and resumed at a later time.
A coroutine is an instance of suspendable computation. It is conceptually similar to a thread, in the sense that it takes a block of code to run that works concurrently with the rest of the code. 
> However, a coroutine is not bound to any particular thread. It may suspend its execution in one thread and resume in another one.
Coroutines can be thought of as light-weight threads, but there is a number of important differences that make their real-life usage very different from threads.

**IMPORTANT!: A coroutine is a state machine that can be suspended and resumed at a later time. It identifies each suspendable point it makes identifies the state using a big switch case under the hood**

**USE EXCEPTIONS**
> Instead of:
> ```kotlin
> private fun validateTimes(startDate: LocalDate, endDate: LocalDate): String
> {
>   if (startDate.isBefore(endDate).not()) return "Start date must be before end date"
>   ...
>   ...
>   return String.empty
> }
> ```
> Do this:
> ```kotlin
> private fun validateTimes(startDate: LocalDate, endDate: LocalDate): Boolean 
> {
>    if (startDate.isBefore(endDate).not())
>       throw new ErrorCodeException(ErrorCode.START_TIME_MUST_BE_AFTER_END_TIME)
>    ...
>    ...
>    return true
> }
> ```


**Different types of Kotlin Dispatchers**
1. ``Dispatcher.IO``:
    - It starts the coroutine in the IO thread can be used for:
      - Networking 
      - Reading 
      - Writing from the database
      - Reading, or writing to the files 
        - Ex: Fetching data from the database is an IO operation, which is done on the IO thread.
2. ``Dispatcher.Default``:
    - It starts the coroutine in the main thread can be used for:
      - We should choose this when we are planning to do Complex and long-running calculations, which can block the main thread and freeze the UI
        - Ex: Suppose we need to do the `10,000 calculations` and we are doing all these calculations on the UI thread ie main thread, and if we wait for the result or `10,000 calculations`, till that time our main thread would be blocked, and our UI will be frozen, leading to poor user experience. So in this case we need to use the `Default Thread`. 
3. ``Dispatcher.Main``:
    - It starts the coroutine in the main thread. It is mostly used when we need to perform the UI operations within the coroutine, as UI can only be changed from the main thread (also called the UI thread).
4. ``Dispatcher.Unconfined``:
    - As the name suggests unconfined dispatcher is not confined to any specific thread. It executes the initial continuation of a coroutine in the current call-frame and lets the coroutine resume in whatever thread that is used by the corresponding suspending function, without mandating any specific threading policy. 

> **Note:** If needed you may do some background operations with one dispatcher and then switch context
> ```kotlin
> withContext(Dispatchers.IO) {
>   // do some background work
>   // then switch to the UI thread
>   withContext(Dispatchers.Main) {
>     // do some UI work
>  }
> } 
> ``` 
>



