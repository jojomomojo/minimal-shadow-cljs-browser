.PHONY: release

watch:
	@docker-compose exec -w /app/src/work/app app ../../env make app-watch

repl:
	@docker-compose exec -w /app/src/work/app app ../../env make app-repl

release:
	@docker-compose exec -w /app/src/work/app app ../../env make app-release

clean:
	rm -rf node_modules
	rm -rf target
	rm -rf .shadow-cljs

init:
	docker-compose down || true
	docker-compose up -d
	docker-compose exec app make fixroot 2>/dev/null 1>/dev/null || true
	docker-compose exec -w /app/src/work/app app ../../env make install
	$(MAKE) watch

install:
	npm ci
	$(MAKE) static

app-watch:
	./node_modules/.bin/shadow-cljs watch app

app-release:
	rm -rf release/*
	env NODE_ENV=production $(MAKE) static-release
	./node_modules/.bin/shadow-cljs release app-release

app-repl:
	while ! test -f target/main.js; do date; sleep 1; done
	./node_modules/.bin/shadow-cljs cljs-repl app

static:
	$(MAKE) css html

static-release:
	$(MAKE) css-release html-release

css:
	mkdir -p target/css
	node_modules/.bin/postcss src/css/app.css -o target/css/bundle.css

css-release:
	mkdir -p release/css
	node_modules/.bin/postcss src/css/app.css -o release/css/bundle.css

html:
	mkdir -p target
	rsync -ia src/html/. target/.

html-release:
	mkdir -p release
	rsync -ia src/html/. release/.

serve:
	cd target && sudo "$(shell which caddy)" file-server

fixos:
	@echo run manually:
	@echo sudo rm -r -f /Library/Developer/CommandLineTools
	@echo xcode-select --install
	@echo sudo xcode-select --switch /Library/Developer/CommandLineTools

bash:
	@docker-compose exec -w /app/src/work/app app ../../env bash
