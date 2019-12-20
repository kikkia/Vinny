.PHONY: run
run: ## Compile and run the module
        git pull origin master
        docker-compose build
        docker-compose up
        
.PHONY: deploy-master
        git pull origin master
        docker-compose build
        docker-compose down
        docker-compose up
