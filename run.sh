#!/bin/sh
#
# Copyright 2019 Institut Laue–Langevin
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


export PUMA_PCC_DATASOURCE_URL_DEFAULT=
export PUMA_PCC_DATASOURCE_USERNAME_DEFAULT=
export PUMA_PCC_DATASOURCE_PASSWORD_DEFAULT=
export PUMA_PCC_DATASOURCE_SCHEMA_DEFAULT=
export PUMA_PCC_ELASTICSEARCH_HOSTS_DEFAULT=
export PUMA_PCC_ELASTICSEARCH_PORT_DEFAULT=
export PUMA_PCC_ELASTICSEARCH_CLUSTERNAME_DEFAULT=
export PUMA_PCC_ELASTICSEARCH_INDEX_DEFAULT=
export PUMA_PCC_FILES_PATH_DEFAULT=$HOME/puma/files
export PUMA_PCC_FILES_CACHE_PATH_DEFAULT=$HOME/puma/files_old
export PUMA_PCC_TASKMANAGER_THREAD_POOL_SIZE_DEFAULT=16
export PUMA_PCC_RESOLVER_URL_DEFAULT=
export PUMA_PCC_IMPORTER_RECOVER_ON_RESTART_DEFAULT=true
export PUMA_PCC_RESOLVER_RECOVER_ON_RESTART_DEFAULT=false
export PUMA_PCC_DOWNLOADER_RECOVER_ON_RESTART_DEFAULT=false
export PUMA_PCC_ABBY_BATCH_PATH_DEFAULT=$HOME/abby/batch/
export PUMA_PCC_ABBY_INPUT_PATH_DEFAULT=$HOME/abby/input/
export PUMA_PCC_ABBY_OUTPUT_PATH_DEFAULT=$HOME/abby/output/

export PUMA_PCC_SPRING_PROFILE_DEFAULT=dev
export PUMA_PCC_VM_ARGS_DEFAULT=


# Set the environment variables
if [ -z "$PUMA_PCC_DATASOURCE_URL" ]; then
	export PUMA_PCC_DATASOURCE_URL="$PUMA_PCC_DATASOURCE_URL_DEFAULT"
	echo "PUMA_PCC_DATASOURCE_URL set to $PUMA_PCC_DATASOURCE_URL"
else
	echo "PUMA_PCC_DATASOURCE_URL defined as $PUMA_PCC_DATASOURCE_URL"
fi

if [ -z "$PUMA_PCC_DATASOURCE_USERNAME" ]; then
	export PUMA_PCC_DATASOURCE_USERNAME="$PUMA_PCC_DATASOURCE_USERNAME_DEFAULT"
	echo "PUMA_PCC_DATASOURCE_USERNAME set to $PUMA_PCC_DATASOURCE_USERNAME"
else
	echo "PUMA_PCC_DATASOURCE_USERNAME defined as $PUMA_PCC_DATASOURCE_USERNAME"
fi

if [ -z "$PUMA_PCC_DATASOURCE_PASSWORD" ]; then
	export PUMA_PCC_DATASOURCE_PASSWORD="$PUMA_PCC_DATASOURCE_PASSWORD_DEFAULT"
	echo "PUMA_PCC_DATASOURCE_PASSWORD set to $PUMA_PCC_DATASOURCE_PASSWORD"
else
	echo "PUMA_PCC_DATASOURCE_PASSWORD defined as $PUMA_PCC_DATASOURCE_PASSWORD"
fi

if [ -z "$PUMA_PCC_DATASOURCE_SCHEMA" ]; then
	export PUMA_PCC_DATASOURCE_SCHEMA="$PUMA_PCC_DATASOURCE_SCHEMA_DEFAULT"
	echo "PUMA_PCC_DATASOURCE_SCHEMA set to $PUMA_PCC_DATASOURCE_SCHEMA"
else
	echo "PUMA_PCC_DATASOURCE_SCHEMA defined as $PUMA_PCC_DATASOURCE_SCHEMA"
fi

if [ -z "$PUMA_PCC_ELASTICSEARCH_HOSTS" ]; then
	export PUMA_PCC_ELASTICSEARCH_HOSTS="$PUMA_PCC_ELASTICSEARCH_HOSTS_DEFAULT"
	echo "PUMA_PCC_ELASTICSEARCH_HOSTS set to $PUMA_PCC_ELASTICSEARCH_HOSTS"
else
	echo "PUMA_PCC_ELASTICSEARCH_HOSTS defined as $PUMA_PCC_ELASTICSEARCH_HOSTS"
fi

if [ -z "$PUMA_PCC_ELASTICSEARCH_PORT" ]; then
	export PUMA_PCC_ELASTICSEARCH_PORT="$PUMA_PCC_ELASTICSEARCH_PORT"
	echo "PUMA_PCC_ELASTICSEARCH_PORT set to $PUMA_PCC_ELASTICSEARCH_PORT"
else
	echo "PUMA_PCC_ELASTICSEARCH_PORT defined as $PUMA_PCC_ELASTICSEARCH_PORT"
fi

if [ -z "$PUMA_PCC_ELASTICSEARCH_CLUSTERNAME" ]; then
	export PUMA_PCC_ELASTICSEARCH_CLUSTERNAME="$PUMA_PCC_ELASTICSEARCH_CLUSTERNAME"
	echo "PUMA_PCC_ELASTICSEARCH_CLUSTERNAME set to $PUMA_PCC_ELASTICSEARCH_CLUSTERNAME"
else
	echo "PUMA_PCC_ELASTICSEARCH_CLUSTERNAME defined as $PUMA_PCC_ELASTICSEARCH_CLUSTERNAME"
fi

if [ -z "$PUMA_PCC_ELASTICSEARCH_INDEX" ]; then
	export PUMA_PCC_ELASTICSEARCH_INDEX="$PUMA_PCC_ELASTICSEARCH_INDEX"
	echo "PUMA_PCC_ELASTICSEARCH_INDEX set to $PUMA_PCC_ELASTICSEARCH_INDEX"
else
	echo "PUMA_PCC_ELASTICSEARCH_INDEX defined as $PUMA_PCC_ELASTICSEARCH_INDEX"
fi

if [ -z "$PUMA_PCC_FILES_PATH" ]; then
	export PUMA_PCC_FILES_PATH="$PUMA_PCC_FILES_PATH_DEFAULT"
	echo "PUMA_PCC_FILES_PATH set to $PUMA_PCC_FILES_PATH"
else
	echo "PUMA_PCC_FILES_PATH defined as $PUMA_PCC_FILES_PATH"
fi

if [ -z "$PUMA_PCC_FILES_CACHE_PATH" ]; then
	export PUMA_PCC_FILES_CACHE_PATH="$PUMA_PCC_FILES_CACHE_PATH_DEFAULT"
	echo "$PUMA_PCC_FILES_CACHE_PATH set to $PUMA_PCC_FILES_CACHE_PATH"
else
	echo "$PUMA_PCC_FILES_CACHE_PATH defined as $PUMA_PCC_FILES_CACHE_PATH"
fi

if [ -z "$PUMA_PCC_TASKMANAGER_THREAD_POOL_SIZE" ]; then
	export PUMA_PCC_TASKMANAGER_THREAD_POOL_SIZE="$PUMA_PCC_TASKMANAGER_THREAD_POOL_SIZE_DEFAULT"
	echo "PUMA_PCC_TASKMANAGER_THREAD_POOL_SIZE set to $PUMA_PCC_TASKMANAGER_THREAD_POOL_SIZE"
else
	echo "PUMA_PCC_TASKMANAGER_THREAD_POOL_SIZE defined as $PUMA_PCC_TASKMANAGER_THREAD_POOL_SIZE"
fi

if [ -z "$PUMA_PCC_RESOLVER_URL" ]; then
	export PUMA_PCC_RESOLVER_URL="$PUMA_PCC_RESOLVER_URL_DEFAULT"
	echo "PUMA_PCC_RESOLVER_URL set to $PUMA_PCC_RESOLVER_URL"
else
	echo "PUMA_PCC_RESOLVER_URL defined as $PUMA_PCC_RESOLVER_URL"
fi

if [ -z "$PUMA_PCC_IMPORTER_RECOVER_ON_RESTART" ]; then
	export PUMA_PCC_IMPORTER_RECOVER_ON_RESTART="$PUMA_PCC_IMPORTER_RECOVER_ON_RESTART_DEFAULT"
	echo "PUMA_PCC_IMPORTER_RECOVER_ON_RESTART set to $PUMA_PCC_IMPORTER_RECOVER_ON_RESTART"
else
	echo "PUMA_PCC_IMPORTER_RECOVER_ON_RESTART defined as $PUMA_PCC_IMPORTER_RECOVER_ON_RESTART"
fi

if [ -z "$PUMA_PCC_RESOLVER_RECOVER_ON_RESTART" ]; then
	export PUMA_PCC_RESOLVER_RECOVER_ON_RESTART="$PUMA_PCC_RESOLVER_RECOVER_ON_RESTART_DEFAULT"
	echo "PUMA_PCC_RESOLVER_RECOVER_ON_RESTART set to $PUMA_PCC_RESOLVER_RECOVER_ON_RESTART"
else
	echo "PUMA_PCC_RESOLVER_RECOVER_ON_RESTART defined as $PUMA_PCC_RESOLVER_RECOVER_ON_RESTART"
fi

if [ -z "$PUMA_PCC_DOWNLOADER_RECOVER_ON_RESTART" ]; then
	export PUMA_PCC_DOWNLOADER_RECOVER_ON_RESTART="$PUMA_PCC_DOWNLOADER_RECOVER_ON_RESTART_DEFAULT"
	echo "PUMA_PCC_DOWNLOADER_RECOVER_ON_RESTART set to $PUMA_PCC_DOWNLOADER_RECOVER_ON_RESTART"
else
	echo "PUMA_PCC_DOWNLOADER_RECOVER_ON_RESTART defined as $PUMA_PCC_DOWNLOADER_RECOVER_ON_RESTART"
fi

if [ -z "$PUMA_PCC_SPRING_PROFILE" ]; then
	export PUMA_PCC_SPRING_PROFILE="$PUMA_PCC_SPRING_PROFILE_DEFAULT"
	echo "PUMA_PCC_SPRING_PROFILE set to $PUMA_PCC_SPRING_PROFILE"
else
	echo "PUMA_PCC_SPRING_PROFILE defined as $PUMA_PCC_SPRING_PROFILE"
fi

if [ -z "$PUMA_PCC_VM_ARGS" ]; then
	export PUMA_PCC_VM_ARGS="$PUMA_PCC_VM_ARGS_DEFAULT"
	echo "PUMA_PCC_VM_ARGS set to $PUMA_PCC_VM_ARGS"
else
	echo "PUMA_PCC_VM_ARGS defined as $PUMA_PCC_VM_ARGS"
fi

if [ -z "$PUMA_PCC_ABBY_BATCH_PATH" ]; then
	export PUMA_PCC_ABBY_BATCH_PATH="$PUMA_PCC_ABBY_BATCH_PATH_DEFAULT"
	echo "PUMA_PCC_ABBY_BATCH_PATH set to $PUMA_PCC_ABBY_BATCH_PATH"
else
	echo "PUMA_PCC_ABBY_BATCH_PATH defined as $PUMA_PCC_ABBY_BATCH_PATH"
fi

if [ -z "$PUMA_PCC_ABBY_INPUT_PATH" ]; then
	export PUMA_PCC_ABBY_INPUT_PATH="$PUMA_PCC_ABBY_INPUT_PATH_DEFAULT"
	echo "PUMA_PCC_ABBY_INPUT_PATH set to $PUMA_PCC_ABBY_INPUT_PATH"
else
	echo "PUMA_PCC_ABBY_INPUT_PATH defined as $PUMA_PCC_ABBY_INPUT_PATH"
fi

if [ -z "$PUMA_PCC_ABBY_OUTPUT_PATH" ]; then
	export PUMA_PCC_ABBY_OUTPUT_PATH="$PUMA_PCC_ABBY_OUTPUT_PATH_DEFAULT"
	echo "PUMA_PCC_ABBY_OUTPUT_PATH set to $PUMA_PCC_ABBY_OUTPUT_PATH"
else
	echo "PUMA_PCC_ABBY_OUTPUT_PATH defined as $PUMA_PCC_ABBY_OUTPUT_PATH"
fi


# Run the application
java $PUMA_PCC_VM_ARGS -Dspring.profiles.active="$PUMA_PCC_SPRING_PROFILE_DEFAULT" -jar puma-app/target/puma-corpus-creator.jar
