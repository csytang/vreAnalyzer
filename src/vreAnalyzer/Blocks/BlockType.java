package vreAnalyzer.Blocks;

public enum BlockType {
	NON{

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return "NON";
		}
		
	},
	
	Stmt{
		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return "Stmt_Block";
		}
	},
	Method{
		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return "Method";
		}
	},
	Class{
		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return "Class";
		}
	},
	Package{
		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return "Package";
		}
	}
}
