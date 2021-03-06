.PHONY: release

repl:
	@docker-compose exec -w /app/src/work app ../env make app-repl

recreate:
	kitt recreate
	$(MAKE) copy
	docker-compose exec -w /app/src/work app ../env make install
	$(MAKE) watch

setup:
	npm install
	docker-compose exec app sudo chown app:app /app/src/.m2 /app/src/work
	docker-compose exec -w /app/src/work app ../env make clean
	$(MAKE) recreate

watch:
	@docker-compose exec -T -w /app/src/work app ../env make app-watch

copy:
	rsync -ia --blocking-io -e "docker-compose exec -T app" \
		--exclude target --exclude release --exclude node_modules --exclude .shadow-cljs \
		. env:work

sync:
	rsync -ia --blocking-io -e "docker-compose exec -T app" \
		--exclude target --exclude release --exclude node_modules --exclude .shadow-cljs \
		--exclude .git \
		env:work/. .

release:
	@docker-compose exec -w /app/src/work app ../env make app-release
	@rsync -ia --blocking-io -e "docker-compose exec -T app" \
		--exclude target --exclude release --exclude node_modules --exclude .shadow-cljs \
		env:work/release/. release/.

clean:
	rm -rf node_modules
	rm -rf target release
	rm -rf .shadow-cljs


install:
	npm ci
	$(MAKE) static

app-watch:
	./node_modules/.bin/shadow-cljs watch app

app-release:
	rm -rf release/*
	mkdir -p release
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

fixos:
	@echo run manually:
	@echo sudo rm -r -f /Library/Developer/CommandLineTools
	@echo xcode-select --install
	@echo sudo xcode-select --switch /Library/Developer/CommandLineTools

bash:
	@docker-compose exec -w /app/src/work app ../env bash
