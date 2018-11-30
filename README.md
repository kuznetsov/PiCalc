# PiCalc
Realtime pi value computation

Based on [Chudnovsky pi caclulation algorithm](https://www.craig-wood.com/nick/articles/pi-chudnovsky/)

![alt text](https://www.craig-wood.com/nick/images/math/13e67da9779a237b0d3b4b0fa5d70d12.png)

Used [Java implementation](https://github.com/lemmingapex/ChudnovskyAlgorithm)

Imported as separate module. Converted Java to Kotlin and added memoization to improve the speed.

Сonducted a series of experiments and found out that single threaded memoization algorithm version is the fastest one.


## Time tests

```kotlin
for (i in 1..k) {
    alg.caclulatePi(i) // depends on tested algorithm version
}
```

### k = 100

                        | Single threaded test | Multithreaded test
----------------------- | -------------------- | ------------------
**Without memoization** | Time = 0.713 sec     | Time = 0.598 sec
**With memoization**    | Time = 0.489 sec     | Time = 0.444 sec

### k = 300

                        | Single threaded test | Multithreaded test
----------------------- | -------------------- | ------------------
**Without memoization** | Time = 3.748 sec     | Time = 3.631 sec
**With memoization**    | Time = 2.713 sec     | Time = 3.105 sec

### k = 500

                        | Single threaded test | Multithreaded test
----------------------- | -------------------- | ------------------
**Without memoization** | Time = 15.272 sec     | Time = 16.775 sec
**With memoization**    | Time = 13.308 sec     | Time = 13.988 sec

### k = 1000

                        | Single threaded test | Multithreaded test
----------------------- | -------------------- | ------------------
**Without memoization** | Time = 72.927 sec     | Time = 70.779 sec
**With memoization**    | Time = 62.032 sec     | Time = 66.412 sec
