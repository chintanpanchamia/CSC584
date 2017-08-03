

public class Node
	{
		int ID; //nodeInteger
		Connection parent; //connection to retrace path
		//ArrayList<Integer> connections; //getConnections()
		int costSoFar; //csf
		int estimatedTotalCost; //etc
		int category; // -1 for closed, 0 for unvisited, 1 for open


		Node(int ident)
		{
			this.ID = ident;
			this.parent = new Connection();
			this.costSoFar = 0;
			this.estimatedTotalCost = 0;
			this.category = 0;

		}

		int getID()
		{
			return this.ID;
		}

		Connection getParent()
		{
			return this.parent;
		}

		void setParent(Connection parent)
		{
			this.parent = parent;
		}

		int getCSF()
		{
			return this.costSoFar;
		}

		void setCSF(int csf)
		{
			this.costSoFar = csf; 
		}

		int getETC()
		{
			return this.estimatedTotalCost;
		}

		void setETC(int etc)
		{
			this.estimatedTotalCost = etc;
		}

		int getCategory()
		{
			return this.category;
		}

		void setCategory(int category)
		{
			this.category = category;
		}


	}