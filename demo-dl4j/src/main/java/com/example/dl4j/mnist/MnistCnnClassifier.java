package com.example.dl4j.mnist;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.datavec.api.io.labels.ParentPathLabelGenerator;
import org.datavec.api.split.FileSplit;
import org.datavec.image.loader.NativeImageLoader;
import org.datavec.image.recordreader.ImageRecordReader;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.PoolingType;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.nd4j.linalg.schedule.MapSchedule;
import org.nd4j.linalg.schedule.ScheduleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.dl4j.utilities.DataUtilities;


public class MnistCnnClassifier {

	private final static Logger log = LoggerFactory.getLogger(MnistCnnClassifier.class);
	private final static String basePath = Paths.get(System.getProperty("java.io.tmpdir"), "mnist").toString();
	private final static String dataUrl = "http://github.com/myleott/mnist_png/raw/master/mnist_png.tar.gz";
	
	public static void main(String[] args) throws Exception {
		//图片高度
		int height = 28;
		//图片宽度
	    int width = 28;
	    //图片色道数(黑白单色道)
	    int channels = 1;
	    //输出数字大小，最后的分类数
	    int outputNum = 10;
	    //每次训练的样本数
	    int batchSize = 54;
	    //训练次数
	    int epochs = 1;
	    
	    //随机种子值，确保训练时初始权重值一致
	    int seed = 1234;
	    Random randNumGen = new Random(seed);
	    
	    log.info("Data load and vectorization...");
	    
	    //读取数据，没有就下载
	    String localFilePath = Paths.get(basePath, "mnist_png.tar.gz").toString(); 
	    if (DataUtilities.downloadFile(dataUrl, localFilePath)) {
	    	log.debug("Data downloaded from {}", dataUrl);
	    }
	    
	    if (Files.notExists(Paths.get(basePath, "mnist_png"), LinkOption.NOFOLLOW_LINKS)) {
	    	 DataUtilities.extractTarGz(localFilePath, basePath);
	    }
	    
	    //将下载好的图片转成训练数据
	    File trainData = Paths.get(basePath, "mnist_png", "training").toFile();
	    FileSplit trainSplit = new FileSplit(trainData, NativeImageLoader.ALLOWED_FORMATS, randNumGen);
	    ParentPathLabelGenerator labelMaker = new ParentPathLabelGenerator();
	    ImageRecordReader trainRR = new ImageRecordReader(height, width, channels, labelMaker);
	    trainRR.initialize(trainSplit);
	    DataSetIterator mnistTrain = new RecordReaderDataSetIterator(trainRR, batchSize, 1, outputNum);

	    //将像素值从0-255转换成0-1
	    DataNormalization scaler = new ImagePreProcessingScaler(0, 1);
	    scaler.fit(mnistTrain);
	    mnistTrain.setPreProcessor(scaler);
	    
	    //将下载好的图片转成测试数据
	    File testData = Paths.get(basePath, "mnist_png", "testing").toFile();
	    FileSplit testSplit = new FileSplit(testData, NativeImageLoader.ALLOWED_FORMATS, randNumGen);
	    ImageRecordReader testRR = new ImageRecordReader(height, width, channels, labelMaker);
	    testRR.initialize(testSplit);
	    DataSetIterator mnistTest = new RecordReaderDataSetIterator(testRR, batchSize, 1, outputNum);
	    mnistTest.setPreProcessor(scaler);
	    
	    log.info("Network configuration and training...");
	    //分阶段生成训练学习速率
	    Map<Integer, Double> lrSchedule = new HashMap<>();
	    lrSchedule.put(0, 0.06);
	    lrSchedule.put(200, 0.05);
	    lrSchedule.put(600, 0.028);
	    lrSchedule.put(800, 0.0060);
	    lrSchedule.put(1000, 0.001);
	    
	    log.info("Build model....");
	    //配置模型属性
	    MultiLayerConfiguration config = new NeuralNetConfiguration.Builder()
	    		//初始化随机权重值
                .seed(seed)
                //L2正则化避免过拟合(L1是加固定参数的绝对值，l2则是加固定参数的平方)
                .l2(0.0005)
                //优化器选择随机梯度下降算法
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                //权重初始化模式
                .weightInit(WeightInit.XAVIER)
                //权重更新
                .updater(new Nesterovs(new MapSchedule(ScheduleType.ITERATION, lrSchedule)))
                //指定网络中层的数量，
                .list()
                //卷积层#1: 单位大小为5的提取器，从一个图片中提取特征，输出深度为32的特征集合，
                //并加以ReLU的激活函数(ReLU: f(x) = max(0, x)小于0则取0，大于0则取本身)
                .layer(0, new ConvolutionLayer.Builder(5, 5)
                        .nIn(channels)
                        .stride(1, 1)
                        .nOut(32)
                        .activation(Activation.RELU)
                        .build())
                //池化层#1: 使用最大池化模式，使用单位大小为2，每隔2步提取特征中的最大值，最后将提取数据缩小一半
                .layer(1, new SubsamplingLayer.Builder(PoolingType.MAX)
                        .kernelSize(2, 2)
                        .stride(2, 2)
                        .build())
                //卷积层#2: 重复卷积层#1的动作，将输出特征集合深度扩展是64
                .layer(2, new ConvolutionLayer.Builder(5, 5)
                        .stride(1, 1)
                        .nOut(64)
                        .activation(Activation.RELU)
                        .build())
                //池化层#2: 重复池化层#2的动作，将特征集合再缩小一半
                .layer(3, new SubsamplingLayer.Builder(PoolingType.MAX)
                        .kernelSize(2, 2)
                        .stride(2, 2)
                        .build())
                //密集层: 建立一个包含1024个神经元的密集层，将所有的特征集合连接在一起
                .layer(4, new DenseLayer.Builder().activation(Activation.RELU)
                        .nOut(1024)
                        .build())
                //逻辑层: 通过损失函数计算误差，最终输出包含了10个神经元的神经层
                //这10个数值则是输入数据掉入到每个神经元上的概率值
                .layer(5, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nOut(outputNum)
                        .activation(Activation.SOFTMAX)
                        .build())
                //输入值转换为[28,28,1]的形状
                .setInputType(InputType.convolutionalFlat(28, 28, 1))
                .backprop(true).pretrain(false).build();

        MultiLayerNetwork model = new MultiLayerNetwork(config);
        model.init();

        log.info("Train model....");
        model.setListeners(new ScoreIterationListener(10));
        log.debug("Total num of params: {}", model.numParams());
        for (int i = 0; i < epochs; i++) {
        	//开始训练
            model.fit(mnistTrain);
            log.info("*** Completed epoch {} ***", i);
            log.info("Evaluate model....");
            //模型测试
            Evaluation eval = model.evaluate(mnistTest);
            //输出最终结果
            log.info(eval.stats());
            mnistTest.reset();
        }
        //将训练好的模型数据存入到文件中。
        ModelSerializer.writeModel(model, Paths.get(basePath, "minist-model.zip").toFile(), true);
        
        System.out.println("base path: " + basePath);
	}
}
