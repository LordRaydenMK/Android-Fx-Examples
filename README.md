# [DEPRECATED] Android <3 Arrow-Fx

![Android Pull Request & Master CI](https://github.com/LordRaydenMK/Android-Fx/workflows/Android%20Pull%20Request%20&%20Master%20CI/badge.svg)

Solving real world Android problems using Arrow-Fx and functional programming. Inspired by the awesome [Kotlin Coroutine Use Cases on Android][coroutines-android].

## DEPRECATION

This repository uses Arrow 0.10.5. The current active version (0.13.X) no longer include the IO datatype. See https://www.47deg.com/blog/arrow-0.13.0-release/

## Arrow and Arrow-Fx

[Λrrow][arrow] is a family of libraries for Typed Functional Programming in Kotlin. In arrow-core we can find data types like `Option` or `Either` that can helps us represent the absence of value and errors respectively. However to do something useful on Android we usually need to talk to our backend, update the UI, update a local database etc. This is where [Arrow-Fx][arrow-fx] comes into play. From the website:

> Arrow Fx is a next-generation Typed FP Effects Library that makes effectful and polymorphic programming first class in Kotlin, and acts as an extension to the Kotlin native suspend system.
> The library brings purity, referential transparency, and direct imperative syntax to typed FP in Kotlin, and is a fun and easy tool for creating Typed Pure Functional Programs. 
> Arrow Fx programs run unmodified in multiple supported frameworks and runtimes such as Arrow Effects IO, KotlinX Coroutines Deferred, Rx2 Observable, and many others.

## The App

The examples use `ViewModel` and `LiveData` from Jetpack and the Arrow-Fx library. The `IO` type from Arrow-Fx works nicely with `suspend` functions. The examples use Arrow-Fx's integration with KotlinX Coroutines to take advantage of the ViewModel Scope for cancellation. Each `Activity` delegates the interesting logic to a `ViewModel`, which does some job and then updates the state hosed in `LiveData`. The `Activity` observes the `LiveData` and updates the UI.

The data layer consists of `GithubService`, a interface that in a real app would be implemented by `Retrofit` using the built-in suspended support. Here I have a mock implementation that simulates delay then returns a pre defined data. It also simulates some (random) errors. The return value of the service is `RepositoryDto` representing arrow's Github repo in a totally fake way.

Tests are written using JUnit4 and the [LiveData Testing][livedata-testing] library (inspired by RxJava's `TestObserver`). The tests also use the real dispatchers (except Android's main thread).

## About the IO<A> data type

In Arrow-Fx `IO<A>` is a data type that represents computation that, *when executed* can succeed with a value of type `A` or fail with a `Throwable`. The key point here is *when executed*, meaning `IO` is lazy. It is a *pure* a value that can be assigned to a variable, passed as a function argument and composed with other values of type `IO`. And it will not do anything until explicitly executed. Typically this is done as late as possible. What we usually do is writing small `IO` programs, then using the `fx` block and other operators provided by `Arrow-Fx` to build larger `IO` programs. You can find out more about Arrow-Fx in the [documentation][arrow-fx].

## Examples

1. [Executing an API call](#executing-an-api-call)
2. [Executing sequential API calls](#executing-sequential-api-calls)
3. [Concurrent API calls](#concurrent-api-calls)
4. [Executing X API calls (serially or concurrently)](#executing-x-api-calls-serially-or-concurrently)
5. [Executing API calls with timeouts](#executing-api-calls-with-timeouts)
6. [Retrying API calls](#retrying-api-calls)
7. [A combination of retry and timeout](#a-combination-of-retry-and-timeout)
8. [Heavy computations in the background](#heavy-computations-in-the-background)
9. [Background operation that outlives the screen](#background-operation-that-outlives-the-screen)
10. [In memory data caching](#in-memory-data-caching)

*Note*: Examples 1-7 are in the same order as the *Kotlin Coroutine Use Cases on Android* project. Examples 8 and 9 exist there with a different number.

### Executing an API call

Uses the `GithubService` to get information about the Arrow repository showing a progress bar while the (fake) request is executed. Upon completion we see the UI with content or an error view (with option to retry the call) in case the call failed (the service randomly throws). 
Operations inside the `fx` block are executed sequentially, first we update the UI, then do the API call, finally convert the data to our ViewState. Here we can see the integration of `Arrow-Fx` and Kotlin's suspend system. Using `effect {}` we can call any suspended function. Inside the `fx` block, if any of the operations fail, the whole computation short circuits and the `IO` fails. Using `handleError` (or similar function) we can recover from that error and provide a value, in this case `ViewState.Error` containing the error message. The result (Content or Error) then is posted to the `LiveData`. The `unsafeRunXXX` (there are multiple variation depending on the use case) function is used to execute the IO and tie the lifecycle of the IO with the `viewModelScope` for automatic cancellation. The result of the `IO` is ignore here because the UI is already updated and all errors handled.

[Check the code here][ex-01]

### Executing Sequential API calls

Uses the `GithubService` to call the API, then uses an ID from the first call to do a second API call and show that information in the UI. Again we display progress during both calls, and show an error if any of them fails. To execute the API calls sequentially, we put them one after the other in the `fx` block (just like the UI update and API call are sequenced). Inside the `fx` block if any operation fails, the whole computation short circuits, or with other words if we go to step 2 it means step 1 succeeded. So when we do the second API call we have access to the value from the first call. The rest of the code is the same as the first example.

[Check the code here][ex-02]

###  Concurrent API calls

Uses the `GithubService` to execute three *independent* API calls concurrently. To do that we can use the `IO.parTupledN` function and pass it the three calls. If all three calls succeed, we get a `Tuple3` consisting of the values for each call. If any of the calls fail, `IO.parTupledN` will return a failed IO with the first failure, and try to cancel the other calls. The code for updating the UI and error handling is similar to the other examples.

[Check the code here][ex-03]

### Executing X API calls (serially or concurrently)

Give a list of UUIDs, it uses `GithubService` to call `getRepositoryDetails` with the ID as parameter. Collects all the results in a list (I'm displaying only the first in the UI because I'm lazy). The concurrent implementation uses `list.parTraverse` to execute the requests. This will execute the request in parallel then add the responses to a list. If any of the requests fail, the whole computation fails. If all requests succeed we get the resulting `List`. 

The alternative implementation uses `list.traverse` to execute the requests one by one. Similarly if any of the requests fail, the whole computation fails. If all requests succeed we get the resulting `List`. The code for updating the UI and error handling is similar to the other examples.

[Check the code here][ex-04]
    
### Executing API calls with timeouts

Uses the `GithubService` to execute an API call with timeout. If the request takes more than X time, the operation fails. To achieve the timeout the `waitFor` function is applied with the desired `Duration` as parameter. The first request will always fail in this example because of timeout. With each retry the `Duration` passed is increased by 1s. The rest of the code is similar to the other examples.

Note: In case of API calls with Retroift, the Timeout support from OkHttp is probably a better fit, however timeouts can be applied to any IO.

[Check the code here][ex-05]
    
### Retrying API calls

API calls can fail, network can be slow or unstable, the server can be down. To ensure good user experience we can retry the calls.

This is where FP really shines. Arrow-Fx comes with the [Schedule][schedule] data type that can be used to define policies for retrying or repeating requests. The definition of the retry policy is separate from the operation being executed, and each policy can be further combined with other policies. I can use `Schedule.spaced(60.seconds)` for a spaced schedule with a value of 60 seconds. I can use `Schedule.recurs(100)` to repeat immediately up to 100 times. I can compose those two schedules into one that repeats with 60s delay up to 100 times using the `and` operator like this: `Schedule.spaced<A>(60.seconds) and Schedule.recurs(100)`. The `and` operator combines two schedules, continues only when both continue and chooses the maximum delay.

Once the schedule is defined any IO value can be retried using `io.retry(IO.concurrent(), mySchedule)`. Another area where FP shines is it goes beyond languages and frameworks. Arrow's schedule is a port of [ZIO Schedule][zio-schedule] from Scala and I recommend [this talk][zio-schedule-youtube] about it.

This sample contains two different schedules, a simple exponential one and one more complex that includes exponential backoff, then spaced and the whole thing has a bit of randomness added.

[Check the code here][ex-06]

### A combination of retry and timeout

A call to `waitFor` provides the timeout, then call `retry` with a schedule to supply a retrying mechanism. 

[Check the code here][ex-07]

### Heavy computations in the background

Offloading a heavy computation in the background is similar to doing a I/O operation in the background, the difference is passing different `Dispatcher`. For computations we can use `continueOn(Dispatchers.Default)`. Note: Arrow-Fx works both with the dispatchers from KotlinX coroutines and it's own dispatchers. Find out more [here][io-dispatchers].

[Check the code here][ex-08]

### Background operation that outlives the screen

Running an `IO` using the coroutines integration means that the IO will be canceled once the `ViewMode.onCleared` is called. Sometimes we don't want to do that. To achieve that we can call `fork()` on our `IO` operation and ignore the tokens for join and cancel (the return values from `fork`). By doing that, that IO operation will run in a separate Fiber and it won't be canceled if/when the main IO is canceled. Note: in case the IO fails we also don't get notified. 

[Check the code here][ex-09]

### In memory data caching

The first time you open the screen, the data will come from the service and be stored in an In Memory cache. The second time the screen is opened the data comes instantly from the cache. There is a button to clear the cache and the next time the screen is opened it will fetch from the service.

The cache implementation use the `Ref` data type that provides a safe concurrent access and modification of it's state. Access and modification is done using `IO`. The tests use a fake implementation that return a user specified value.

## Contributing

This repo is still a WIP. If you have an idea to improve some example feel free to create an issue or open a PR. If you have questions about how the code works I recommend [StackOverflow][so-arrow] and make sure to tag it with `arrow-kt`.

## Acknowledgements

- [LukasLechnerDev](https://github.com/LukasLechnerDev) for writing Kotlin-Coroutine-Use-Cases-on-Android which inspired this project
- The [Arrow contributors](https://github.com/arrow-kt/arrow/graphs/contributors) for writing an amazing library that makes writing FP programs with Kotlin possible
- [47 Degrees](https://www.47deg.com/) for sponsoring Λrrow


[coroutines-android]: https://github.com/LukasLechnerDev/Kotlin-Coroutine-Use-Cases-on-Android
[arrow]: https://arrow-kt.io/
[arrow-fx]: https://arrow-kt.io/docs/fx/
[livedata-testing]: https://github.com/jraska/livedata-testing
[ex-01]: /app/src/main/java/io/github/lordraydenmk/android_fx/example01/SingleApiCallViewModel.kt
[ex-02]: /app/src/main/java/io/github/lordraydenmk/android_fx/example02/SequentialApiCallsViewModel.kt
[ex-03]: /app/src/main/java/io/github/lordraydenmk/android_fx/example03/ConcurrentApiCallsViewModel.kt
[ex-04]: /app/src/main/java/io/github/lordraydenmk/android_fx/example04/MultipleNetworkRequestsViewModel.kt
[ex-05]: /app/src/main/java/io/github/lordraydenmk/android_fx/example05/RequestWithTimeoutViewModel.kt
[schedule]: https://arrow-kt.io/docs/0.10/apidocs/arrow-fx/arrow.fx/-schedule/
[zio-schedule]: https://zio.dev/docs/datatypes/datatypes_schedule
[zio-schedule-youtube]: https://www.youtube.com/watch?v=onQSHiafAY8
[ex-06]: /app/src/main/java/io/github/lordraydenmk/android_fx/example06/RetryingViewModel.kt
[ex-07]: /app/src/main/java/io/github/lordraydenmk/android_fx/example07/RetryAndTimeoutViewModel.kt
[io-dispatchers]: https://stackoverflow.com/questions/61711204/io-dispatchers-vs-kotlinx-dispatchers-inside-an-fx-block
[ex-08]: /app/src/main/java/io/github/lordraydenmk/android_fx/example08/ComputeInBackgroundViewModel.kt
[ex-09]: /app/src/main/java/io/github/lordraydenmk/android_fx/example09/OutliveScreenViewModel.kt
[so-arrow]: https://stackoverflow.com/questions/tagged/arrow-kt
