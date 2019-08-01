# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

from __future__ import print_function

import arrow
import datetime
import json
import os
import taskcluster

from lib.util import (
    convert_camel_case_into_kebab_case,
    lower_case_first_letter,
)

DEFAULT_EXPIRES_IN = '1 year'
_OFFICIAL_REPO_URL = 'https://github.com/mozilla-mobile/reference-browser'
_DEFAULT_TASK_URL = 'https://queue.taskcluster.net/v1/task'

class TaskBuilder(object):
    def __init__(
        self,
        task_id,
        repo_url,
        git_ref,
        short_head_branch,
        commit,
        owner,
        source,
        scheduler_id,
        date_string,
        tasks_priority='lowest',
        trust_level=1
    ):
        self.task_id = task_id
        self.repo_url = repo_url
        self.git_ref = git_ref
        self.short_head_branch = short_head_branch
        self.commit = commit
        self.owner = owner
        self.source = source
        self.scheduler_id = scheduler_id
        self.tasks_priority = tasks_priority
        self.date = arrow.get(date_string)
        self.trust_level = trust_level

    def craft_assemble_nightly_task(self, variant, version_name, is_staging):
        artifacts = _craft_artifacts_from_variant(variant)

        sentry_secret = '{}project/mobile/reference-browser/sentry'.format(
            'garbage/staging/' if is_staging else ''
        )

        pre_gradle_commands = (
            'python automation/taskcluster/helper/get-secret.py -s {} -k {} -f {}'.format(
                sentry_secret, 'dsn', '.sentry_token'
            ),
        )

        gradle_commands = (
            './gradlew --no-daemon -PcrashReportEnabled=true -Ptelemetry=true -PversionName={} '
            'clean test assembleNightly'.format(version_name),
        )

        command = ' && '.join(
            cmd
            for commands in (pre_gradle_commands, gradle_commands)
            for cmd in commands
            if cmd
        )

        routes = [] if is_staging else [
            "notify.email.fenix-eng-notifications@mozilla.com.on-failed"
        ]

        return self._craft_build_ish_task(
            name='assemble: {}'.format(variant.name),
            description='Building and testing variant {}'.format(variant.name),
            command=command,
            scopes=[
                "secrets:get:{}".format(sentry_secret)
            ],
            artifacts=artifacts,
            routes=routes,
            treeherder={
                'jobKind': 'build',
                'machine': {
                  'platform': 'android-all',
                },
                'symbol': 'NA',
                'tier': 1,
            },
        )

    def craft_assemble_task(self, variant):
        return self._craft_clean_gradle_task(
            name='assemble: {}'.format(variant.name),
            description='Building variant {}'.format(variant.name),
            gradle_task='assemble{}'.format(variant.build_type),
            artifacts=_craft_artifacts_from_variant(variant),
            treeherder={
                'groupSymbol': variant.build_type,
                'jobKind': 'build',
                'machine': {
                  'platform': 'android-all',
                },
                'symbol': 'A',
                'tier': 1,
            },
        )

    def craft_test_task(self, variant):
        return self._craft_clean_gradle_task(
            name='test: {}'.format(variant.name),
            description='Building and testing variant {}'.format(variant.name),
            gradle_task='test{}UnitTest'.format(variant.build_type),
            treeherder={
                'groupSymbol': variant.build_type,
                'jobKind': 'test',
                'machine': {
                  'platform': 'android-all',
                },
                'symbol': 'T',
                'tier': 1,
            },
        )

    def craft_detekt_task(self):
        return self._craft_clean_gradle_task(
            name='detekt',
            description='Running detekt over all modules',
            gradle_task='detekt',
            treeherder={
                'jobKind': 'test',
                'machine': {
                  'platform': 'lint',
                },
                'symbol': 'detekt',
                'tier': 1,
            }

        )

    def craft_ktlint_task(self):
        return self._craft_clean_gradle_task(
            name='ktlint',
            description='Running ktlint over all modules',
            gradle_task='ktlint',
            treeherder={
                'jobKind': 'test',
                'machine': {
                  'platform': 'lint',
                },
                'symbol': 'ktlint',
                'tier': 1,
            }
        )

    def craft_lint_task(self):
        return self._craft_clean_gradle_task(
            name='lint',
            description='Running ktlint over all modules',
            gradle_task='lintDebug',
            treeherder={
                'jobKind': 'test',
                'machine': {
                  'platform': 'lint',
                },
                'symbol': 'lint',
                'tier': 1,
            },
        )

    def _craft_clean_gradle_task(
        self, name, description, gradle_task, artifacts=None, routes=None, treeherder=None
    ):
        return self._craft_build_ish_task(
            name=name,
            description=description,
            command='./gradlew --no-daemon clean {}'.format(gradle_task),
            artifacts=artifacts,
            routes=routes,
            treeherder=treeherder,
        )

    def craft_compare_locales_task(self):
        return self._craft_build_ish_task(
            name='compare-locales',
            description='Validate strings.xml with compare-locales',
            command=(
                'pip install "compare-locales>=5.0.2,<6.0" && '
                'compare-locales --validate l10n.toml .'
            ),
            treeherder={
                'jobKind': 'test',
                'machine': {
                  'platform': 'lint',
                },
                'symbol': 'compare-locale',
                'tier': 2,
            }
        )

    def craft_ui_tests_task(self):
        artifacts = {
            "public": {
                "type": "directory",
                "path": "/build/reference-browser/results",
                "expires": taskcluster.stringDate(taskcluster.fromNow(DEFAULT_EXPIRES_IN))
            }
        }

        env_vars = {
            "GOOGLE_PROJECT": "moz-reference-browser-230023",
            "GOOGLE_APPLICATION_CREDENTIALS": ".firebase_token.json"
        }

        gradle_commands = (
            './gradlew --no-daemon clean assembleDebug assembleAndroidTest',
        )

        test_commands = (
            'automation/taskcluster/androidTest/ui-test.sh aarch64 -1',
            'automation/taskcluster/androidTest/ui-test.sh arm -1',
        )

        command = ' && '.join(
            cmd
            for commands in (gradle_commands, test_commands)
            for cmd in commands
            if cmd
        )

        return self._craft_build_ish_task(
            name='UI tests',
            description='Execute Gradle tasks for UI tests',
            command=command,
            scopes=[
                'secrets:get:project/mobile/reference-browser/firebase'
            ],
            artifacts=artifacts,
            env_vars=env_vars,
        )

    def _craft_build_ish_task(
        self, name, description, command, dependencies=None, artifacts=None, scopes=None,
        routes=None, treeherder=None, env_vars=None
    ):
        dependencies = [] if dependencies is None else dependencies
        artifacts = {} if artifacts is None else artifacts
        scopes = [] if scopes is None else scopes
        routes = [] if routes is None else routes
        env_vars = {} if env_vars is None else env_vars 

        full_command = ' && '.join([
            'export TERM=dumb',
            'cd .. ',
            'git clone {}'.format(self.repo_url),
            'cd reference-browser',
            'git fetch {} {} --tags'.format(self.repo_url, self.git_ref),
            'git config advice.detachedHead false',
            'git checkout {}'.format(self.commit),
            command
        ])

        features = {}
        if artifacts:
            features['chainOfTrust'] = True
        if any(scope.startswith('secrets:') for scope in scopes):
            features['taskclusterProxy'] = True

        payload = {
            "features": features,
            "env": env_vars, 
            "maxRunTime": 7200,
            # TODO Stop using this docker image
            "image": "mozillamobile/android-components:1.17",
            "command": [
                "/bin/bash",
                "--login",
                "-cx",
                full_command
            ],
            "artifacts": artifacts,
        }

        return self._craft_default_task_definition(
            worker_type='mobile-{}-b-ref-browser'.format(self.trust_level),
            provisioner_id='aws-provisioner-v1',
            name=name,
            description=description,
            payload=payload,
            dependencies=dependencies,
            routes=routes,
            scopes=scopes,
            treeherder=treeherder,
        )

    def _craft_default_task_definition(
        self,
        worker_type,
        provisioner_id,
        name,
        description,
        payload,
        dependencies=None,
        routes=None,
        scopes=None,
        treeherder=None,
    ):
        dependencies = [] if dependencies is None else dependencies
        scopes = [] if scopes is None else scopes
        routes = [] if routes is None else routes
        treeherder = {} if treeherder is None else treeherder

        created = datetime.datetime.now()
        deadline = taskcluster.fromNow('1 day')
        expires = taskcluster.fromNow(DEFAULT_EXPIRES_IN)

        if self.trust_level == 3:
            routes.append("tc-treeherder.v2.reference-browser.{}".format(self.commit))

        return {
            "provisionerId": provisioner_id,
            "workerType": worker_type,
            "taskGroupId": self.task_id,
            "schedulerId": self.scheduler_id,
            "created": taskcluster.stringDate(created),
            "deadline": taskcluster.stringDate(deadline),
            "expires": taskcluster.stringDate(expires),
            "retries": 5,
            "tags": {},
            "priority": self.tasks_priority,
            "dependencies": [self.task_id] + dependencies,
            "requires": "all-completed",
            "routes": routes,
            "scopes": scopes,
            "payload": payload,
            "extra": {
                "treeherder": treeherder,
            },
            "metadata": {
                "name": "Reference-Browser - {}".format(name),
                "description": description,
                "owner": self.owner,
                "source": self.source,
            },
        }

    def craft_raptor_signing_task(self, assemble_task_id, variant, is_staging):
        staging_prefix = '.staging' if is_staging else ''
        routes = [
            "index.project.mobile.reference-browser.v3{}.raptor.{}.{}.{}.latest".format(
                staging_prefix, self.date.year, self.date.month, self.date.day
            ),
            "index.project.mobile.reference-browser.v3{}.raptor.{}.{}.{}.revision.{}".format(
                staging_prefix, self.date.year, self.date.month, self.date.day, self.commit
            ),
            "index.project.mobile.reference-browser.v3{}.raptor.latest".format(staging_prefix),
        ]

        return self._craft_signing_task(
            name='sign: {}'.format(variant.name),
            description='Dep-signing variant {}'.format(variant.name),
            signing_type='dep-signing',
            assemble_task_id=assemble_task_id,
            apk_paths=variant.taskcluster_apk_paths,
            routes=routes,
            treeherder={
                'groupSymbol': variant.build_type,
                'jobKind': 'other',
                'machine': {
                    'platform': 'android-all',
                },
                'symbol': 'As',
                'tier': 1,
            },
        )

    def craft_nightly_signing_task(self, build_task_id, variant, is_staging):
        index_release = 'staging.nightly' if is_staging else 'nightly'
        routes = [
            'index.project.mobile.reference-browser.v2.{}.{}.{}.{}.latest'.format(
                index_release, self.date.year, self.date.month, self.date.day
            ),
            'index.project.mobile.reference-browser.v2.{}.{}.{}.{}.revision.{}'.format(
                index_release, self.date.year, self.date.month, self.date.day, self.commit
            ),
            'index.project.mobile.reference-browser.v2.{}.latest'.format(index_release),
        ]

        return self._craft_signing_task(
            name='Signing task',
            description='Sign nightly builds of reference-browser',
            signing_type='dep-signing' if is_staging else 'release-signing',
            assemble_task_id=build_task_id,
            apk_paths=['public/target.{}.apk'.format(apk.abi) for apk in variant.apks],
            routes=routes,
            treeherder={
                'jobKind': 'other',
                'machine': {
                  'platform': 'android-all',
                },
                'symbol': 'Ns',
                'tier': 1,
            },
        )

    def _craft_signing_task(
        self, name, description, signing_type, assemble_task_id, apk_paths, routes, treeherder
    ):
        signing_format = 'autograph_apk_reference_browser'
        payload = {
            'upstreamArtifacts': [{
                'paths': apk_paths,
                'formats': [signing_format],
                'taskId': assemble_task_id,
                'taskType': 'build'
            }]
        }

        return self._craft_default_task_definition(
            worker_type='mobile-signing-dep-v1' if signing_type.startswith('dep-') else 'mobile-signing-v1',
            provisioner_id='scriptworker-prov-v1',
            dependencies=[assemble_task_id],
            routes=routes,
            scopes=[
                "project:mobile:reference-browser:releng:signing:format:{}".format(signing_format),
                "project:mobile:reference-browser:releng:signing:cert:{}".format(signing_type),
            ],
            name=name,
            description=description,
            payload=payload,
            treeherder=treeherder,
        )

    def craft_push_task(self, signing_task_id, variant, is_staging):
        payload = {
            "commit": True,
            "channel": 'nightly',
            "upstreamArtifacts": [
                {
                    "paths": variant.taskcluster_apk_paths,
                    "taskId": signing_task_id,
                    "taskType": "signing"
                }
            ]
        }

        return self._craft_default_task_definition(
            worker_type='mobile-pushapk-dep-v1' if is_staging else 'mobile-pushapk-v1',
            provisioner_id='scriptworker-prov-v1',
            name="Push task",
            description="Upload signed release builds of Reference-Browser to Google Play",
            payload=payload,
            dependencies=[signing_task_id],
            scopes=[
                "project:mobile:reference-browser:releng:googleplay:product:reference-browser{}".format(
                    ':dep' if is_staging else ''
                )
            ],
            treeherder={
                'jobKind': 'other',
                'machine': {
                  'platform': 'android-all',
                },
                'symbol': 'gp',
                'tier': 1,
            },
        )

    def craft_upload_apk_nimbledroid_task(self, assemble_task_id):
        # For GeckoView, upload nightly (it has release config) by default, all Release builds have WV
        return self._craft_build_ish_task(
            name="Upload Release APK to Nimbledroid",
            description='Upload APKs to Nimbledroid for performance measurement and tracking.',
            command=' && '.join([
                'curl --location "{}/{}/artifacts/public/target.arm.apk" > target.arm.apk'.format(_DEFAULT_TASK_URL, assemble_task_id),
                'python automation/taskcluster/upload_apk_nimbledroid.py',
            ]),
            treeherder={
                'jobKind': 'test',
                'machine': {
                  'platform': 'android-all',
                },
                'symbol': 'compare-locale',
                'tier': 2,
            },
            scopes=["secrets:get:project/mobile/reference-browser/nimbledroid"],
            dependencies=[assemble_task_id],
        )

    def craft_raptor_speedometer_task(self, abi, signing_task_id, mozharness_task_id, variant, gecko_revision, force_run_on_64_bit_device=False):
        return self._craft_raptor_task(
            abi,
            signing_task_id,
            mozharness_task_id,
            variant,
            gecko_revision,
            name_prefix='raptor speedometer',
            description='Raptor Speedometer on the Reference Browser',
            test_name='raptor-speedometer',
            job_symbol='sp',
            force_run_on_64_bit_device=force_run_on_64_bit_device,
        )

    def craft_raptor_speedometer_power_task(self, abi, signing_task_id, mozharness_task_id, variant, gecko_revision, force_run_on_64_bit_device=False):
        return self._craft_raptor_task(
            abi,
            signing_task_id,
            mozharness_task_id,
            variant,
            gecko_revision,
            name_prefix='raptor speedometer power',
            description='Raptor Speedometer power on the Reference Browser',
            test_name='raptor-speedometer',
            job_symbol='sp',
            group_symbol='Rap-P',
            extra_test_args=[
                "--power-test",
                "--page-cycles 5",
                "--host HOST_IP",
            ],
            force_run_on_64_bit_device=force_run_on_64_bit_device,
        )

    def craft_raptor_tp6m_task(self, for_suite):

        def craft_function(abi, signing_task_id, mozharness_task_id, variant, gecko_revision, force_run_on_64_bit_device=False):
            return self._craft_raptor_task(
                abi,
                signing_task_id,
                mozharness_task_id,
                variant,
                gecko_revision,
                name_prefix='raptor tp6m-{}'.format(for_suite),
                description='Raptor tp6m on the Reference Browser',
                test_name='raptor-tp6m-{}'.format(for_suite),
                job_symbol='tp6m-{}'.format(for_suite),
                force_run_on_64_bit_device=force_run_on_64_bit_device,
            )
        return craft_function

    def _craft_raptor_task(
        self,
        abi,
        signing_task_id,
        mozharness_task_id,
        variant,
        gecko_revision,
        name_prefix,
        description,
        test_name,
        job_symbol,
        group_symbol=None,
        extra_test_args=None,
        force_run_on_64_bit_device=False,
    ):
        extra_test_args = [] if extra_test_args is None else extra_test_args
        apk_location = '{}/{}/artifacts/public/target.{}.apk'.format(
            _DEFAULT_TASK_URL, signing_task_id, abi
        )
        worker_type = 'gecko-t-bitbar-gw-perf-p2' if force_run_on_64_bit_device or abi == 'arm64-v8a' else 'gecko-t-bitbar-gw-perf-g5'

        if force_run_on_64_bit_device:
            treeherder_platform = 'android-hw-p2-8-0-arm7-api-16'
        elif abi == 'armeabi-v7a':
            treeherder_platform = 'android-hw-g5-7-0-arm7-api-16'
        elif abi == 'arm64-v8a':
            treeherder_platform = 'android-hw-p2-8-0-android-aarch64'
        else:
            raise ValueError('Unsupported architecture "{}"'.format(abi))

        task_name = '{}: {} {} {}'.format(
            name_prefix, variant.name, abi, '(on 64-bit-device)' if force_run_on_64_bit_device else ''
        )

        return self._craft_default_task_definition(
            worker_type=worker_type,
            provisioner_id='proj-autophone',
            dependencies=[signing_task_id],
            name=task_name,
            description=description,
            payload={
                "maxRunTime": 2700,
                "artifacts": [{
                    'path': worker_path,
                    'expires': taskcluster.stringDate(taskcluster.fromNow(DEFAULT_EXPIRES_IN)),
                    'type': 'directory',
                    'name': 'public/{}/'.format(public_folder)
                } for worker_path, public_folder in (
                    ('artifacts/public', 'test'),
                    ('workspace/logs', 'logs'),
                    ('workspace/build/blobber_upload_dir', 'test_info'),
                )],
                "command": [[
                    "/builds/taskcluster/script.py",
                    "bash",
                    "./test-linux.sh",
                    "--cfg=mozharness/configs/raptor/android_hw_config.py",
                    "--test={}".format(test_name),
                    "--app=refbrow",
                    "--binary=org.mozilla.reference.browser.raptor",
                    "--activity=org.mozilla.reference.browser.GeckoViewActivity",
                    "--download-symbols=ondemand"
                ] + extra_test_args],
                "env": {
                    "EXTRA_MOZHARNESS_CONFIG": json.dumps({
                        "test_packages_url": "{}/{}/artifacts/public/build/en-US/target.test_packages.json".format(_DEFAULT_TASK_URL, mozharness_task_id),
                        "installer_url": apk_location,
                    }),
                    "GECKO_HEAD_REPOSITORY": "https://hg.mozilla.org/mozilla-central",
                    "GECKO_HEAD_REV": gecko_revision,
                    "MOZ_AUTOMATION": "1",
                    "MOZ_HIDE_RESULTS_TABLE": "1",
                    "MOZ_NO_REMOTE": "1",
                    "MOZ_NODE_PATH": "/usr/local/bin/node",
                    "MOZHARNESS_CONFIG": "raptor/android_hw_config.py",
                    "MOZHARNESS_SCRIPT": "raptor_script.py",
                    "MOZHARNESS_URL": "{}/{}/artifacts/public/build/en-US/mozharness.zip".format(_DEFAULT_TASK_URL, mozharness_task_id),
                    "MOZILLA_BUILD_URL": apk_location,
                    "NEED_XVFB": "false",
                    "NO_FAIL_ON_TEST_ERRORS": "1",
                    "SCCACHE_DISABLE": "1",
                    "TASKCLUSTER_WORKER_TYPE": worker_type[len('gecko-'):],
                    "XPCOM_DEBUG_BREAK": "warn",
                },
                "mounts": [{
                    "content": {
                        "url": "https://hg.mozilla.org/mozilla-central/raw-file/{}/taskcluster/scripts/tester/test-linux.sh".format(gecko_revision),
                    },
                    "file": "test-linux.sh",
                }]
            },
            treeherder={
                'jobKind': 'test',
                'groupSymbol': 'Rap' if group_symbol is None else group_symbol,
                'machine': {
                  'platform': treeherder_platform,
                },
                'symbol': job_symbol,
                'tier': 2,
            }
        )


def _craft_artifacts_from_variant(variant):
    return {
        apk.taskcluster_path: {
            'type': 'file',
            'path': apk.absolute_path(variant.build_type),
            'expires': taskcluster.stringDate(taskcluster.fromNow(DEFAULT_EXPIRES_IN)),
        } for apk in variant.apks
    }


def schedule_task(queue, taskId, task):
    print("TASK", taskId)
    print(json.dumps(task, indent=4, separators=(',', ': ')))

    result = queue.createTask(taskId, task)
    print("RESULT", taskId)
    print(json.dumps(result))


def schedule_task_graph(ordered_groups_of_tasks):
    queue = taskcluster.Queue({'baseUrl': 'http://taskcluster/queue/v1'})
    full_task_graph = {}

    # TODO: Switch to async python to speed up submission
    for group_of_tasks in ordered_groups_of_tasks:
        for task_id, task_definition in group_of_tasks.items():
            schedule_task(queue, task_id, task_definition)

            full_task_graph[task_id] = {
                # Some values of the task definition are automatically filled. Querying the task
                # allows to have the full definition. This is needed to make Chain of Trust happy
                'task': queue.task(task_id),
            }

    return full_task_graph


def fetch_mozharness_task_id():
    # We now want to use the latest available raptor
    raptor_index = 'gecko.v2.mozilla-central.nightly.latest.mobile.android-x86_64-opt'
    return taskcluster.Index().findTask(raptor_index)['taskId']
