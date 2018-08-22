package com.example.dl4j.tutorial;

import org.deeplearning4j.datasets.iterator.impl.EmnistDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.eval.ROCMultiClass;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

public class QuickStartExample {
    public static void main(String[] args) throws Exception {
        //一次训练抓取的数据
        int batchSize = 16;
        EmnistDataSetIterator.Set emnistSet = EmnistDataSetIterator.Set.BALANCED;
        DataSetIterator emnistTrain = new EmnistDataSetIterator(emnistSet, batchSize, true);
        DataSetIterator emnistTest = new EmnistDataSetIterator(emnistSet, batchSize, false);

        //获取最终输出的分类数
        int outputNum = EmnistDataSetIterator.numLabels(emnistSet);
        int rngSeed = 123;
        //图片的高度及宽度。也就是各自包含的像素点数
        int numRows = 28;
        int numColumns = 28;

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(rngSeed)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Adam())
                .l2(1e-4)
                .list()
                .layer(0, new DenseLayer.Builder()
                        //输入的数据点数
                        .nIn(numRows * numColumns)
                        //输出数据点数
                        .nOut(1000)
                        //激活函数
                        .activation(Activation.RELU)
                        //权重值初始化
                        .weightInit(WeightInit.XAVIER)
                        .build())
                .layer(1, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nIn(1000)
                        .nOut(outputNum)
                        .activation(Activation.SOFTMAX)
                        .weightInit(WeightInit.XAVIER)
                        .build())
                .build();

        MultiLayerNetwork network = new MultiLayerNetwork(conf);
        network.init();

        //每隔5个迭代就输出一次训练分数
        int eachIterations = 5;
        network.addListeners(new ScoreIterationListener(eachIterations));

        //单次训练
        network.fit(emnistTrain, 2);

        Evaluation eval = network.evaluate(emnistTest);
        System.out.println("Accuracy: " + eval.accuracy());
        System.out.println("Precision: " + eval.precision());
        System.out.println("Recall: " + eval.recall());

        //评估操作者曲线及计算曲线下面积
        ROCMultiClass roc = network.evaluateROCMultiClass(emnistTest);
        System.out.println("AverageAUC: " + roc.calculateAverageAUC());

        int classIndex = 0;
        System.out.println("AUC: " + roc.calculateAUC(classIndex));

        System.out.println(eval.stats());
        System.out.println(roc.stats());
    }
}
