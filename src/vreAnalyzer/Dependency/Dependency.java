package vreAnalyzer.Dependency;

public interface Dependency {
	enum VariabilityDependency implements Dependency{
		Optional,Mandatory
	}
	enum ConstaintsDependency implements Dependency{
		requires,excludes
	}
}
