develop: 
	npm install
	rm -rf target
	$(MAKE) static
	./node_modules/.bin/shadow-cljs watch app

repl:
	./node_modules/.bin/shadow-cljs cljs-repl app

static: css js
	true

css:
	mkdir -p target/css
	node_modules/.bin/postcss src/css/app.css -o target/css/bundle.css

js:
	mkdir -p target/js
	node_modules/.bin/webpack

release:
	rm -rf target
	mkdir -p target
	./node_modules/.bin/shadow-cljs release app
	npm run serve

fixos:
	@echo run manually:
	@echo sudo rm -r -f /Library/Developer/CommandLineTools
	@echo xcode-select --install
	@echo sudo xcode-select --switch /Library/Developer/CommandLineTools
