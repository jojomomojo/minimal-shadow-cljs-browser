develop: 
	npm install
	rm -rf target
	$(MAKE) css
	./node_modules/.bin/shadow-cljs watch app

release:
	rm -rf target
	env NODE_ENV=production $(MAKE) css
	./node_modules/.bin/shadow-cljs release app

css:
	mkdir -p target/css
	node_modules/.bin/postcss src/css/app.css -o target/css/bundle.css

repl:
	./node_modules/.bin/shadow-cljs cljs-repl app

fixos:
	@echo run manually:
	@echo sudo rm -r -f /Library/Developer/CommandLineTools
	@echo xcode-select --install
	@echo sudo xcode-select --switch /Library/Developer/CommandLineTools
