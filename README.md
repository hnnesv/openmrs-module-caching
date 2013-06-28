OpenMRS Module - Caching
========================

*ALPHA VERSION*

Provides caching services for OpenMRS.

Overview
--------
The CachingService API provides methods for caching and retrieving objects and has a flexible caching back-end. The supported back-ends are:
* Memcached
* Ehcache

The module also caches several standard OpenMRS API object types:
* Encounter types
* Ok, that's only one type. But more types coming soon!

Dependencies
------------
* Serialization Xstream 0.2.7
