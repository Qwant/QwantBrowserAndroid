#  Qwant Browser

A web browser implementation using [Mozilla Android Components](https://github.com/mozilla-mobile/android-components).

Code is strongly inspired from [Mozilla Reference Browser](https://github.com/mozilla-mobile/reference-browser).
It uses the Gecko engine through GeckoView.


Enjoy the first European search engine directly on your mobile phone. Qwant gives you a neutral access to the whole Web, the latest news and social media content. We do not collect your personal data, we truly respect your privacy. No cookie, no behavorial profiling. You can browse the web without being tracked.

### Instant answers on top of your search results

With Qwant Search, get the exact answers to your queries displayed instantly below the search bar. Weather forecasting or the next movie screening are all available on the top of the search results.
No need to scroll anymore to find the right information. Find everything at first glance.

### The first search engine dedicated to music

Be aware of the latest news and releases from your favourite band instantly by using Qwant Search. To go further, Qwant Music offers an intuitive interface entirely dedicated to artists and their fans. You will never miss the live concert of the year anymore.

### The search engine for videogames

Qwant Search displays the latest news and information about your trendy favourite video game on the top of the search results. Qwant Games gives you the opportunity to discover exclusive content :detailed description, ratings, live Twitch videos, etc. This is the perfect place to become the games master.

### A tool respectful of your digital privacy

Choosing Qwant is using a search engine that respects its users. We do not keep any track of your queries. No one is able to know what you are looking for on the Internet. Your search results are not tracked. You browse the web without filters and any targeted ads.

# Google Play Store and Test Channel

[Download from Playstore](https://play.google.com/store/apps/details?id=com.qwant.liberty)

You can join the Beta test channel 

# Download APK Directly

Signed builds can be downloaded for every released version from our [Release page](https://github.com/qwant/qwantbrowser/releases):

# Getting Involved

We encourage you to participate in this open source project. We love pull requests, bug reports, ideas, (security) code reviews or any kind of positive contribution.

* [View current Issues](https://github.com/qwant/qwantbrowser/issues) or [View current Pull Requests](https://github.com/qwant/qwantbrowser/pulls).

# Local Development

Building the sources is all done within Android studio:
- Checkout the code
- Open the root folder as an existing project with android-studio
- DO IT AND CHECK !!! build

## Dependency substitutions

You might be interested in building this project against local versions of some of the dependencies.
This could be done either by using a [local maven repository](https://mozilla-mobile.github.io/android-components/contributing/testing-components-inside-app) (quite cumbersome), or via Gradle's [dependency substitutions](https://docs.gradle.org/current/userguide/customizing_dependency_resolution_behavior.html) (not at all cumbersome!).

Currently, the substitution flow is streamlined for some of the core dependencies via configuration flags in `local.properties`. You can build against a local checkout of the following dependencies by specifying their local paths:
- [application-services](https://github.com/mozilla/application-services), specifying its path via `substitutions.application-services.dir=../application-services`
  - This assumes that you have an `application-services` project at the same level in the directory hierarchy as the `reference-browser`.
- [GeckoView](https://hg.mozilla.org/mozilla-central), specifying its path via `dependencySubstitutions.geckoviewTopsrcdir=/path/to/mozilla-central` (and, optionally, `dependencySubstitutions.geckoviewTopobjdir=/path/to/topobjdir`). See [Bug 1533465](https://bugzilla.mozilla.org/show_bug.cgi?id=1533465).
  - This assumes that you have built, packaged, and published your local GeckoView -- but don't worry, the dependency substitution script has the latest instructions for doing that.

Do not forget to run a Gradle sync in Android Studio after changing `local.properties`. If you specified any substitutions, they will be reflected in the modules list, and you'll be able to modify them from a single Android Studio window.

# License

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/
