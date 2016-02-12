DEPENDENCIES_EROCCI_DBUS_JAVA="/home/christophe/workspace/erocci-dbus-java/lib"

# Install Eclipse OCL jars into the local repo.
mvn install:install-file -Dfile="$DEPENDENCIES_EROCCI_DBUS_JAVA/org.eclipse.ocl.pivot-1.0.1.v20150908-1239.jar" \
	                         -DpomFile="$DEPENDENCIES_EROCCI_DBUS_JAVA/org.eclipse.ocl.pivot-1.0.1.v20150908-1239.pom"


mvn install:install-file -Dfile="$DEPENDENCIES_EROCCI_DBUS_JAVA/org.eclipse.ocl.common-1.3.0.v20150519-0914.jar" \
	                         -DpomFile="$DEPENDENCIES_EROCCI_DBUS_JAVA/org.eclipse.ocl.common-1.3.0.v20150519-0914.pom"

mvn install:install-file -Dfile="$DEPENDENCIES_EROCCI_DBUS_JAVA/org.occiware.clouddesigner.parent-0.1.0-SNAPSHOT.pom" \
	                         -DgroupId=Clouddesigner \
	                         -DartifactId=org.occiware.clouddesigner.parent \
	                         -Dversion=0.1.0-SNAPSHOT \
	                         -Dpackaging=pom \

mvn install:install-file -Dfile="$DEPENDENCIES_EROCCI_DBUS_JAVA/org.occiware.clouddesigner.occi-0.1.0-SNAPSHOT.jar" \
	                         -DpomFile="$DEPENDENCIES_EROCCI_DBUS_JAVA/org.occiware.clouddesigner.occi-0.1.0-SNAPSHOT.pom"

