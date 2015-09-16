package Patch.Hadoop.ReuseAssets;

public enum AssetType {
	NON{

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return "NON";
		}
		
	},
	Field{
		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return "Field";
		}
	},
	Argument{
		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return "Argument";
		}
	},
	Local{
		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return "Local";
		}
	},
	Stmt{
		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return "Stmt";
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
