development:
	npm install
	rm -rf target
	mkdir -p target
	rsync -ia assets/index.html target/
	./node_modules/.bin/shadow-cljs watch app

repl:
	./node_modules/.bin/shadow-cljs cljs-repl app

release:
	rm -rf target
	mkdir -p target
	rsync -ia assets/index.html target/
	./node_modules/.bin/shadow-cljs release app
	npm run serve
