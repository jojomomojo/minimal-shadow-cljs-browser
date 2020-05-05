start:
	@docker-compose exec -w /app/src/work/app app ../../env make watch

repl:
	@docker-compose exec -w /app/src/work/app app ../../env make cljs-repl

bundle:
	@docker-compose exec -w /app/src/work/app app ../../env make release

init:
	@docker-compose exec app make fixroot 2>/dev/null 1>/dev/null || true
	@docker-compose exec -w /app/src/work/app app ../../env make install

install:
	rm -rf target
	npm install
	$(MAKE) css
	$(MAKE) html

watch:
	./node_modules/.bin/shadow-cljs watch app

release:
	rm -rf target
	env NODE_ENV=production $(MAKE) static
	./node_modules/.bin/shadow-cljs release app

static:
	$(MAKE) css html

css:
	mkdir -p target/css
	node_modules/.bin/postcss src/css/app.css -o target/css/bundle.css

html:
	mkdir -p target
	rsync -ia src/html/. target/.

serve:
	cd target && sudo "$(shell which caddy)" file-server

cljs-repl:
	while ! test -f target/main.js; do date; sleep 1; done
	./node_modules/.bin/shadow-cljs cljs-repl app

fixos:
	@echo run manually:
	@echo sudo rm -r -f /Library/Developer/CommandLineTools
	@echo xcode-select --install
	@echo sudo xcode-select --switch /Library/Developer/CommandLineTools

bash:
	@docker-compose exec -w /app/src/work/app app ../../env bash

clean:
	rm -rf node_modules
	rm -rf target
	rm -rf .shadow-cljs

