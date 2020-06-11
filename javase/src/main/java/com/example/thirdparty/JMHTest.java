package com.example.thirdparty;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/*
 * 指定Benchmark模式
 * Throughput: 整体吞吐量,表示指定时间内能执行多少次调用。分数越高越好
 * AverageTime: 调用的平均时间，表示每次调用方法平均消耗的时间。
 * SampleTime: 随机取样，最后输出取样结果的分布。
 * SingleShotTime: 不预热，运行一次，用于测试冷启动的性能。
 */
@BenchmarkMode(Mode.Throughput)
//表示同样的Benchmark需要跑几次
@Fork(2)
//测试线程数，一般为CPU*2
@Threads(4)
//预热次数，可以设置每次预热时间
@Warmup(iterations = 2, time = 800, timeUnit = TimeUnit.MICROSECONDS)
//执行测试轮次，可以设置每次预热时间
@Measurement(iterations = 4, time = 1)
//测试结果输出时间单位
@OutputTimeUnit(TimeUnit.MICROSECONDS)
//指定一个状态类，用于传递状态的共享范围，主要用于传递测试参数，以及开始@Setup/结束@TearDown方法
//Benchmark: 此次测试共享数据。 Thread: 各测试线程之间不共享数据。
@State(Scope.Benchmark)
public class JMHTest {
    //指定测试参数
    @Param({"sss"})
    public String s = null;

    public String other() {
        //System.out.println("call other method");
        return "nothing";
    }

    //指定需要测试的方法
    @Benchmark
    public String orElseMethod() {
        return Optional.ofNullable(s).orElse(other());
    }

    @Benchmark
    public String orElseGetMethod() {
        return Optional.ofNullable(s).orElseGet(() -> other());
    }

    public static void main(String[] args) throws Exception {
        Options options = new OptionsBuilder()
                .include(JMHTest.class.getSimpleName())
                /*
                .threads(4)                                                             //等同于@Threads
                .forks(2)                                                               //等同于@forks
                .warmupIterations(4).warmupTime(TimeValue.seconds(1))                   //等同于@Warmup
                .measurementIterations(8).measurementTime(TimeValue.milliseconds(500))  //Measurement
                .output("")     //指定输出结果到文件
                */
                .build();
        new Runner(options).run();
    }
}
