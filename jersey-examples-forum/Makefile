ARTIFACT=jersey-examples-forum

all: build

.PHONY: build
build:
	@install -d src/main/webapp/assets/lib
	@cp -R bower_components/bootstrap/dist src/main/webapp/assets/lib/bootstrap
	@cp -R bower_components/jquery/dist src/main/webapp/assets/lib/jquery
	@install -d tmp
	@install -d upload

.PHONY: clean
clean:
	@rm -rf tmp
	@rm -rf upload
	@rm -rf bower_components/
	@rm -rf src/main/webapp/assets/lib
	@rm -rf src/main/resources/conf
	@rm -rf src/main/webapp/META-INF
	@rm -rf src/main/resources/META-INF/persistence.xml

.PHONY: development
development:
	@cp -R skel/development/resources/conf src/main/resources/.
	@cp skel/development/resources/META-INF/persistence.xml src/main/resources/META-INF/.

.PHONY: tomcat 
tomcat:
	@cp -R skel/tomcat/resources/conf src/main/resources/.
	@cp skel/tomcat/resources/META-INF/persistence.xml src/main/resources/META-INF/.
	@install -d src/main/webapp/META-INF
	@cp skel/tomcat/webapp/META-INF/context.xml src/main/webapp/META-INF/.

bower_components:
	@bower install

