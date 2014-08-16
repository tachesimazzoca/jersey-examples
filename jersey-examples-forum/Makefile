ARTIFACT=jersey-examples-forum

all: build

.PHONY: build
build: bower_components
	@install -d src/main/webapp/assets/lib
	@cp -R bower_components/bootstrap/dist src/main/webapp/assets/lib/bootstrap
	@cp -R bower_components/jquery/dist src/main/webapp/assets/lib/jquery

.PHONY: clean
clean:
	@rm -rf src/main/webapp/assets/lib
	@rm -rf bower_components/

bower_components:
	@bower install

