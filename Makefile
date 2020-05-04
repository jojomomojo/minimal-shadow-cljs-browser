watch:
	rm -rf target
	npm install
	$(MAKE) css
	./node_modules/.bin/shadow-cljs watch app

release:
	rm -rf target
	env NODE_ENV=production $(MAKE) css
	./node_modules/.bin/shadow-cljs release app
	rsync -ia src/html/. target/.

css:
	mkdir -p target/css
	node_modules/.bin/postcss src/css/app.css -o target/css/bundle.css

repl:
	while ! test -f target/main.js; do date; sleep 1; done
	open http://localhost:8080
	./node_modules/.bin/shadow-cljs cljs-repl app

fixos:
	@echo run manually:
	@echo sudo rm -r -f /Library/Developer/CommandLineTools
	@echo xcode-select --install
	@echo sudo xcode-select --switch /Library/Developer/CommandLineTools
