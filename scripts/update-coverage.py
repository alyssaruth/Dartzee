import re


if __name__ == '__main__':
    reportLines = open("../build/reports/kover/report.xml").readlines()
    lineCoverageTotals = list(filter(lambda line: "<counter type=\"LINE\"" in line, reportLines))[-1]
    coverageRegex = re.match('^.*missed="(.*)" covered="(.*)"/>$', lineCoverageTotals)
    print(lineCoverageTotals)
    linesMissed = int(coverageRegex.group(1))
    linesCovered = int(coverageRegex.group(2))
    coverage = linesCovered / (linesMissed + linesCovered)
    coverageFormatted = '{:.2%}'.format(coverage)
    print(coverageFormatted)