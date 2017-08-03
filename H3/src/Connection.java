class Connection
	{
		int cost;
		int fromNode;
		int toNode;

		Connection()
		{
			cost = 0;
			fromNode = -1;
			toNode = -1;
		}

		int getCost()
		{
			return this.cost;
		}

		int getFromNode()
		{
			return this.fromNode;
		}

		int getToNode()
		{
			return this.toNode;
		}

		void setCost(int cost)
		{
			this.cost = cost;
		}

		void setFromNode(int fromNode)
		{
			this.fromNode = fromNode;
		}

		void setToNode(int toNode)
		{
			this.toNode = toNode;
		}
	}