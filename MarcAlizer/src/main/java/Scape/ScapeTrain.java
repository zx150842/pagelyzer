package Scape;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import JKernelMachines.fr.lip6.classifier.SMOSVM;
import JKernelMachines.fr.lip6.kernel.typed.DoubleLinear;
import JKernelMachines.fr.lip6.type.TrainingSample;


public class ScapeTrain extends MarcAlizer{
	private boolean isTrain=false;
	private /*public */ArrayList<TrainingSample<double[]>> trainingSamples = new ArrayList<TrainingSample<double[]>>();
	static String params;
	
	
	public void addExampleOfTrain(ArrayList<Double> pairDesc, int label){
		label = label==1 ? 1 : -1;
		/* convert ArrayList in array of double*/
		double []pairDescTrain = new double[pairDesc.size()];
		for(int j=0 ; j<pairDesc.size() ; j++)
			pairDescTrain[j] = pairDesc.get(j).doubleValue();
		
		trainingSamples.add(new TrainingSample<double[]>(pairDescTrain, label));
	}
	
	public void addExampleOfTrain_Img(BufferedImage image1,BufferedImage image2, int label){
		//we ignore the image with label 2
		if(label==2)
			return;
		
		ArrayList<Double> pairDesc = new ArrayList<Double>();
		create_features_visual(image1,image2,pairDesc);

		addExampleOfTrain(pairDesc,label);
	}
	
	public void addExampleOfTrain(String fichierXml1, String fichierXml2, int label){
		//we ignore the image with label 2
		if(label==2)
			return;
		
		ArrayList<Double> pairDesc = new ArrayList<Double>();
		XMLDescriptors.run(fichierXml1,fichierXml2,pairDesc);

		addExampleOfTrain(pairDesc,label);
	}
	
	public void addExampleOfTrain(String fichierXml1, String fichierXml2,BufferedImage image1,BufferedImage image2, int label){
		//we ignore the image with label 2
		if(label==2)
			return;
		
		ArrayList<Double> pairDesc = new ArrayList<Double>();
		create_features_visual(image1,image2,pairDesc);
		XMLDescriptors.run(fichierXml1,fichierXml2,pairDesc);
		
		addExampleOfTrain(pairDesc,label);
	}
	
	public void train(){
		isTrain = true;
		if(!isInialize){
			System.err.println("You need initialize this algorithm for training.");
			return ;
		}
		/* train SVM */
		DoubleLinear kernel = new DoubleLinear();
		svm = new SMOSVM<double[]>(kernel);
		//svm.setC(1);
		svm.setVerbosityLevel(0);

		//////////////////////////////////////////////////////////////////
		/*
		double []means = {0,0,0,0};
		double []sd = {0,0,0,0};
		for(TrainingSample<double[]> ex:trainingSamples){
			for(int i=0 ; i<ex.sample.length ; i++){
				means[i]+=ex.sample[i];
				sd[i]+=ex.sample[i]*ex.sample[i];
			}
		}
		for(int i=0 ; i<4 ; i++){
			means[i]/=4;
			sd[i]-=means[i]*means[i];
		}
		for(TrainingSample<double[]> ex:trainingSamples){
			for(int i=0 ; i<ex.sample.length ; i++){
				ex.sample[i] = (ex.sample[i] -means[i])/Math.sqrt(sd[i]);				
			}
		}*/
		//////////////////////////////////////////////////////////////////
		svm.train(trainingSamples);
		
		/*Evaluator<double[]> evaluator = new Evaluator<double[]>(svm, trainingSamples, trainingSamples);
		System.out.print("...");
		evaluator.evaluate();
		System.out.println("Map: "+evaluator.getTestingMAP());
		*/
	}
	/**
	 * save the SVM after training
	 */
	public void saveSVM(String path){
		if(isTrain){
			try {
				System.out.println("configFile.getBinSVM(): " + path + configFile.getBinSVM());
				FileOutputStream fos = new FileOutputStream( path + configFile.getBinSVM());
				ObjectOutputStream oos = new ObjectOutputStream(fos);
				oos.writeObject(svm);
				oos.close();
			}
			catch (FileNotFoundException e) {e.printStackTrace();} 
			catch (IOException e) {e.printStackTrace();}
			
		}else{
			System.err.println("You must run the save.");
		}
	}
	/**
	 * @usage java ScapeTrain <config.xml> <train.txt> 
	 * @param args
	
	
	public static void main(String[] args) {
		ScapeTrain sc= new ScapeTrain();
		File f = new File(args[0]);
		sc.init(f);
		params = f.getParent();
		
		boolean isImage = f.getName().startsWith("ex_image");
		boolean isHybrid = f.getName().startsWith("ex_hybrid");
		boolean isXml = f.getName().startsWith("ex_structure");
		
		
		FileReader fr=null;
		
		BufferedReader r=null;
		try {
			fr = new FileReader(args[1]);
			r = new BufferedReader(fr);
		}
		catch (FileNotFoundException e) {e.printStackTrace();} 
		
		String parent=(new File(args[1])).getParentFile().getAbsolutePath()+"/";
		System.out.println("parent : " + parent);
		int i=0;
		try {
			while(r.ready()) {
				String []l=r.readLine().split("\t");
				if (l[0].equals("2"))
					continue;
				if (isImage) {
					System.out.println("image pair: n°"+i+" -> "+l[1]+" , "+(i+1)+" -> "+l[2]+"|| label= "+l[0]);
					sc.addExampleOfTrain(ImageIO.read(new File(parent+l[1])), ImageIO.read(new File(parent+l[2])), new Integer(l[0]));
				} else if (isXml) {
					System.out.println("xml pair: n°"+i+" -> "+l[1]+" , "+(i+1)+" -> "+l[2]+"|| label= "+l[0]);
					sc.addExampleOfTrain(parent+l[1], parent+l[2], new Integer(l[0]));					
				} else if (isHybrid) {
					System.out.println("hybrid pair: n°"+i+" -> "+l[1]+" , "+(i+1)+" -> "+l[2]+"|| label= "+l[0]);
					System.out.println(parent+l[1]);
					System.out.println(parent+l[2]);
					sc.addExampleOfTrain(parent+l[1], parent+l[2], ImageIO.read(new File(parent+l[3])), ImageIO.read(new File(parent+l[4])), new Integer(l[0]));
				}
				else 
					System.out.println("Type problem: xml file is not hybrid, image or xml.");
				i+=2;
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		sc.train();
		sc.saveSVM();
	}
	 */
}