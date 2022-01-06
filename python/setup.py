# Copyright (c) Microsoft Corporation.
# Licensed under the MIT license.

import os
import pathlib

from setuptools import find_packages

try:
    from setuptools import setup
except ImportError:
    from distutils.core import setup

# README file from repo root directory
repo_root = str(pathlib.Path(__file__).resolve().parent)

# README file from Feast repo root directory
README_FILE = os.path.join(repo_root, "README.md")
with open(README_FILE, "r") as f:
    LONG_DESCRIPTION = f.read()

setup(
    name="databricks-mosaic",
    author="Databricks",
    version="1.0.0",
    description="Mosaic geospatial analytics (python bindings)",
    url="https://github.com/databricks/mosaic",
    long_description=LONG_DESCRIPTION,
    long_description_content_type="text/markdown",
    python_requires=">=3.7.0",
    packages=find_packages(exclude=("tests",)),
    install_requires=["pyspark>=3.1.1"],
    extras_require={"dev": ["mypy", "isort", "flake8", "black", "build"]},
    # https://stackoverflow.com/questions/28509965/setuptools-development-requirements
    # Install dev requirements with: pip install -e .[dev]
    include_package_data=True,
    license="Apache License 2.0",
    classifiers=[
        # Trove classifiers
        # Full list: https://pypi.python.org/pypi?%3Aaction=list_classifiers
        "License :: OSI Approved :: Apache Software License",
        "Programming Language :: Python",
        "Programming Language :: Python :: 3",
        "Programming Language :: Python :: 3.7",
    ],
)
