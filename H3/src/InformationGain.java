import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class InformationGain
{
	public static void main(String args[]) throws IOException
	{
		String file = "data.csv";
		BufferedReader br = null;
		String line = "";
		String[] attributes;
		double entropy[];
		double informationGain[];
		try
		{
			br = new BufferedReader(new FileReader(file));
			line = br.readLine();
			attributes = line.split(",");
			entropy = new double[(attributes.length - 2)*2 + 1];
			informationGain = new double[attributes.length - 2];
			ArrayList<ArrayList<Integer>> data = new ArrayList<>();
			int followAction = 0, chaseAction = 0, eatAction = 0;
			//TILL FILE HAS NO MORE LINES
			while((line = br.readLine()) != null)
			{
				//FILL UP DATA STRUCTURE
				String[] dataRow = line.split(",");
				ArrayList<Integer> a = new ArrayList<>();
				for(int i = 1; i < dataRow.length - 1; i++)
				{
					a.add(Integer.parseInt(dataRow[i]));
				}
				
				switch(dataRow[dataRow.length - 1])
				{
					case "follow":
					{
						a.add(1);
						followAction++;
						break;
					}
					case "chase":
					{
						a.add(2);
						chaseAction++;
						break;
					}
					case "eat":
					{
						a.add(3);
						eatAction++;
						break;
					}
				}
				
				data.add(a);
			}
			
			System.out.println("follow: " + followAction + " chase: " + chaseAction + " eat: " + eatAction);
			double followFraction = ((double)followAction/data.size()); 
			double chaseFraction = ((double)chaseAction/data.size());
			double eatFraction = ((double)eatAction/data.size());
			entropy[0] = -followFraction * Math.log(followFraction)/Math.log(2);
			entropy[0] -= chaseFraction * Math.log(chaseFraction)/Math.log(2);
			entropy[0] -= eatFraction * Math.log(eatFraction)/Math.log(2);
			//0: E
			//1: E-inPerception
			//2: E-outOfPerception
			//3: E-inEatDistance
			//4: E-outOfEatDistance
			System.out.println("Entropy of Set of Actions: " + entropy[0] + '\n');
			
			int true0 = 0, true1 = 0, true2 = 0, true3 = 0;
			int followAction0 = 0, followAction1 = 0, chaseAction0 = 0, chaseAction1 = 0, eatAction0 = 0, eatAction1 = 0;
			int followAction2 = 0, followAction3 = 0, chaseAction2 = 0, chaseAction3 = 0, eatAction2 = 0, eatAction3 = 0;
			for(int i = 0; i < data.size(); i++)
			{
				ArrayList<Integer> a = data.get(i);
				//System.out.println(a.get(0));
				if(a.get(0) == 1)
				{
					true0++;
					if(a.get(2) == 1)	
						followAction0++;
					else if(a.get(2) == 2)	
						chaseAction0++;
					else	
						eatAction0++;
				}
				else// if(a.get(0) == 0)
				{
					true1++;
					if(a.get(2) == 1)	
						followAction1++;
					else if(a.get(2) == 2)	
						chaseAction1++;
					else	
						eatAction1++;
				}
				
				
				if(a.get(1) == 1)
				{
					true2++;
					if(a.get(2) == 1)	
						followAction2++;
					else if(a.get(2) == 2)	
						chaseAction2++;
					else	
						eatAction2++;
				}
				else// if(a.get(0) == 0)
				{
					true3++;
					if(a.get(2) == 1)	
						followAction3++;
					else if(a.get(2) == 2)	
						chaseAction3++;
					else	
						eatAction3++;
				}
				
			}
			
			double followFraction0 = ((double)followAction0/true0); 
			double chaseFraction0 = ((double)chaseAction0/true0);
			double eatFraction0 = ((double)eatAction0/true0);
			//System.out.println(followFraction0 + " " + chaseFraction0 + " " + eatFraction0);
			
			double followFraction1 = ((double)followAction1/true1); 
			double chaseFraction1 = ((double)chaseAction1/true1);
			double eatFraction1 = ((double)eatAction1/true1);
			//System.out.println(followFraction1 + " " + chaseFraction1 + " " + eatFraction1);
			
			double followFraction2 = ((double)followAction2/true2); 
			double chaseFraction2 = ((double)chaseAction2/true2);
			double eatFraction2 = ((double)eatAction2/true2);
			//System.out.println(followFraction2 + " " + chaseFraction2 + " " + eatFraction2);
			
			double followFraction3 = ((double)followAction3/true3); 
			double chaseFraction3 = ((double)chaseAction3/true3);
			double eatFraction3 = ((double)eatAction3/true3);
			//System.out.println(followFraction3 + " " + chaseFraction3 + " " + eatFraction3);
			
			if(followFraction0 != 0)
				entropy[1] = -followFraction0 * Math.log(followFraction0)/Math.log(2);
			if(chaseFraction0 != 0)
				entropy[1] -= chaseFraction0 * Math.log(chaseFraction0)/Math.log(2);
			if(eatFraction0 != 0)	
				entropy[1] -= eatFraction0 * Math.log(eatFraction0)/Math.log(2);
			
			if(followFraction1 != 0)
				entropy[2] = -followFraction1 * Math.log(followFraction1)/Math.log(2);
			if(chaseFraction1 != 0)	
				entropy[2] -= chaseFraction1 * Math.log(chaseFraction1)/Math.log(2);
			if(eatFraction1 != 0)	
				entropy[2] -= eatFraction1 * Math.log(eatFraction1)/Math.log(2);
			
			System.out.println("E-inPerception: " + entropy[1] + " E-outOfPerception: " + entropy[2]);
			
			if(followFraction2 != 0)
				entropy[3] = -followFraction2 * Math.log(followFraction2)/Math.log(2);
			if(chaseFraction0 != 0)
				entropy[3] -= chaseFraction2 * Math.log(chaseFraction2)/Math.log(2);
			if(eatFraction0 != 0)	
				entropy[3] -= eatFraction2 * Math.log(eatFraction2)/Math.log(2);
			
			if(followFraction3 != 0)
				entropy[4] = -followFraction3 * Math.log(followFraction3)/Math.log(2);
			if(chaseFraction1 != 0)	
				entropy[4] -= chaseFraction3 * Math.log(chaseFraction3)/Math.log(2);
			if(eatFraction1 != 0)	
				entropy[4] -= eatFraction3 * Math.log(eatFraction3)/Math.log(2);
			
			System.out.println("E-inEatingDistance: " + entropy[3] + " E-outOfEatingDistance: " + entropy[4] + '\n');
			
			informationGain[0] = entropy[0] - (double)(true0/data.size()) * entropy[1] - (double)(true1/data.size()) * entropy[2];
			informationGain[1] = entropy[0] - (double)(true2/data.size()) * entropy[3] - (double)(true3/data.size()) * entropy[4];
			
			System.out.println("IG for perception distance: " + informationGain[0]);
			System.out.println("IG for eating distance: " + informationGain[1] + '\n');
			System.out.println("//choosing EATING DISTANCE as ROOT" + '\n');
			
			
			for(int i = 0; i < data.size(); i++)
			{
				ArrayList<Integer> a = data.get(i);
				//System.out.println(a.get(0));
				if(a.get(1) == 0)
					if(a.get(0) == 1)
					{
						true0++;
						if(a.get(2) == 1)	
							followAction0++;
						else if(a.get(2) == 2)	
							chaseAction0++;
						else	
							eatAction0++;
					}
					else// if(a.get(0) == 0)
					{
						true1++;
						if(a.get(2) == 1)	
							followAction1++;
						else if(a.get(2) == 2)	
							chaseAction1++;
						else	
							eatAction1++;
					}
				
			}
			
			followFraction0 = ((double)followAction0/true0); 
			chaseFraction0 = ((double)chaseAction0/true0);
			eatFraction0 = ((double)eatAction0/true0);
			//System.out.println(followFraction0 + " " + chaseFraction0 + " " + eatFraction0);
			
			followFraction1 = ((double)followAction1/true1); 
			chaseFraction1 = ((double)chaseAction1/true1);
			eatFraction1 = ((double)eatAction1/true1);
			//System.out.println(followFraction1 + " " + chaseFraction1 + " " + eatFraction1);
			
			if(followFraction0 != 0)
				entropy[1] = -followFraction0 * Math.log(followFraction0)/Math.log(2);
			if(chaseFraction0 != 0)
				entropy[1] -= chaseFraction0 * Math.log(chaseFraction0)/Math.log(2);
			if(eatFraction0 != 0)	
				entropy[1] -= eatFraction0 * Math.log(eatFraction0)/Math.log(2);
			
			if(followFraction1 != 0)
				entropy[2] = -followFraction1 * Math.log(followFraction1)/Math.log(2);
			if(chaseFraction1 != 0)	
				entropy[2] -= chaseFraction1 * Math.log(chaseFraction1)/Math.log(2);
			if(eatFraction1 != 0)	
				entropy[2] -= eatFraction1 * Math.log(eatFraction1)/Math.log(2);
			
			System.out.println("E-inPerception: " + entropy[1] + " E-outOfPerception: " + entropy[2]);
			informationGain[0] = entropy[0] - (double)(true0/data.size()) * entropy[1] - (double)(true1/data.size()) * entropy[2];
			System.out.println('\n' + "New Information Gain for Perception Distance: " + informationGain[0]);
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			br.close();
		}
	}
}
