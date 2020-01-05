package com.example.dl4j.tutorial;

import com.example.dl4j.utilities.DataUtilities;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.datasets.datavec.RecordReaderMultiDataSetIterator;
import org.deeplearning4j.eval.ROC;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.ComputationGraphConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.graph.MergeVertex;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.MultiDataSetIterator;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CloudDetectionExample {
    public static void main(String[] args) throws Exception {
        // 下载数据
        String DATA_URL = "https://bpstore1.blob.core.windows.net/tutorials/Cloud.tar.gz";
        String DATA_PATH = Paths.get(System.getProperty("java.io.tmpdir"), "dl4j_Cloud").toString();
        String localFilePath = Paths.get(DATA_PATH, "Cloud.tar.gz").toString();
        if (DataUtilities.downloadFile(DATA_URL, localFilePath)) {
            System.out.println("download file from: " + DATA_URL);
        }

        Path dataPath = Paths.get(DATA_PATH, "Cloud");
        if (Files.notExists(dataPath, LinkOption.NOFOLLOW_LINKS)) {
            DataUtilities.extractTarGz(localFilePath, DATA_PATH);
        }

        // 配置数据集
        String trainBaseDir1 = Paths.get(dataPath.toString(), "train", "n1", "train.csv").toString();
        String trainBaseDir2 = Paths.get(dataPath.toString(), "train", "n2", "train.csv").toString();
        String trainBaseDir3 = Paths.get(dataPath.toString(), "train", "n3", "train.csv").toString();
        String trainBaseDir4 = Paths.get(dataPath.toString(), "train", "n4", "train.csv").toString();
        String trainBaseDir5 = Paths.get(dataPath.toString(), "train", "n5", "train.csv").toString();

        String testBaseDir1 = Paths.get(dataPath.toString(), "test", "n1", "test.csv").toString();
        String testBaseDir2 = Paths.get(dataPath.toString(), "test", "n2", "test.csv").toString();
        String testBaseDir3 = Paths.get(dataPath.toString(), "test", "n3", "test.csv").toString();
        String testBaseDir4 = Paths.get(dataPath.toString(), "test", "n4", "test.csv").toString();
        String testBaseDir5 = Paths.get(dataPath.toString(), "test", "n5", "test.csv").toString();

        RecordReader rrTrain1 = new CSVRecordReader(1);
        rrTrain1.initialize(new FileSplit(new File(trainBaseDir1)));
        RecordReader rrTrain2 = new CSVRecordReader(1);
        rrTrain2.initialize(new FileSplit(new File(trainBaseDir2)));
        RecordReader rrTrain3 = new CSVRecordReader(1);
        rrTrain3.initialize(new FileSplit(new File(trainBaseDir3)));
        RecordReader rrTrain4 = new CSVRecordReader(1);
        rrTrain4.initialize(new FileSplit(new File(trainBaseDir4)));
        RecordReader rrTrain5 = new CSVRecordReader(1);
        rrTrain5.initialize(new FileSplit(new File(trainBaseDir5)));

        MultiDataSetIterator train = new RecordReaderMultiDataSetIterator.Builder(20)
                .addReader("rr1",rrTrain1)
                .addReader("rr2",rrTrain2)
                .addReader("rr3",rrTrain3)
                .addReader("rr4",rrTrain4)
                .addReader("rr5",rrTrain5)
                .addInput("rr1", 1, 3)
                .addInput("rr2", 0, 2)
                .addInput("rr3", 0, 2)
                .addInput("rr4", 0, 2)
                .addInput("rr5", 0, 2)
                .addOutputOneHot("rr1", 0, 2)
                .build();

        RecordReader rrTest1 = new CSVRecordReader(1);
        rrTest1.initialize(new FileSplit(new File(testBaseDir1)));
        RecordReader rrTest2 = new CSVRecordReader(1);
        rrTest2.initialize(new FileSplit(new File(testBaseDir2)));
        RecordReader rrTest3 = new CSVRecordReader(1);
        rrTest3.initialize(new FileSplit(new File(testBaseDir3)));
        RecordReader rrTest4 = new CSVRecordReader(1);
        rrTest4.initialize(new FileSplit(new File(testBaseDir4)));
        RecordReader rrTest5 = new CSVRecordReader(1);
        rrTest5.initialize(new FileSplit(new File(testBaseDir5)));

        MultiDataSetIterator test = new RecordReaderMultiDataSetIterator.Builder(20)
                .addReader("rr1",rrTest1)
                .addReader("rr2",rrTest2)
                .addReader("rr3",rrTest3)
                .addReader("rr4",rrTest4)
                .addReader("rr5",rrTest5)
                .addInput("rr1", 1, 3)
                .addInput("rr2", 0, 2)
                .addInput("rr3", 0, 2)
                .addInput("rr4", 0, 2)
                .addInput("rr5", 0, 2)
                .addOutputOneHot("rr1", 0, 2)
                .build();

        ComputationGraphConfiguration conf = new NeuralNetConfiguration.Builder()
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT).seed(12345)
                .updater(new Adam())
                .graphBuilder()
                .addInputs("input1", "input2", "input3", "input4", "input5")
                .addLayer("L1", new DenseLayer.Builder()
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.RELU)
                        .nIn(3).nOut(50)
                        .build(), "input1")
                .addLayer("L2", new DenseLayer.Builder()
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.RELU)
                        .nIn(3).nOut(50)
                        .build(), "input2")
                .addLayer("L3", new DenseLayer.Builder()
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.RELU)
                        .nIn(3).nOut(50)
                        .build(), "input3")
                .addLayer("L4", new DenseLayer.Builder()
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.RELU)
                        .nIn(3).nOut(50)
                        .build(), "input4")
                .addLayer("L5", new DenseLayer.Builder()
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.RELU)
                        .nIn(3).nOut(50)
                        .build(), "input5")
                .addVertex("merge", new MergeVertex(), "L1", "L2", "L3", "L4", "L5")
                .addLayer("L6", new DenseLayer.Builder()
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.RELU)
                        .nIn(250).nOut(125).build(), "merge")
                .addLayer("out", new OutputLayer.Builder()
                        .lossFunction(LossFunctions.LossFunction.MCXENT)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.SOFTMAX)
                        .nIn(125)
                        .nOut(2).build(), "L6")
                .setOutputs("out")
                .pretrain(false).backprop(true)
                .build();

        ComputationGraph model = new ComputationGraph(conf);
        model.init();

        for (int i = 1; i <= 5; i++) {
            System.out.println("Epoch " + i);
            model.fit(train);
        }

        ROC roc = model.evaluateROC(test, 100);
        System.out.println(roc.calculateAUC());
    }
}
