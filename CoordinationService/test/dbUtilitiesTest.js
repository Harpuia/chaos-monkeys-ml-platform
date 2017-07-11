var utilities = require('../routes/dbUtilities');
var should = require('should');

describe('twoDigits() in DB Utilities', function () {
  it('should pad single-digit numbers to two digits', function () {
    var pos = 5;
    var neg = -5;
    var zero = 0;

    var posRet = utilities.twoDigits(pos);
    var negRet = utilities.twoDigits(neg);
    var zeroRet = utilities.twoDigits(zero);

    posRet.should.equal('05');
    negRet.should.equal('-05');
    zeroRet.should.equal('00');
  });

  it('should cast non-single-digit numbers to string', function () {
    var posTwoDigits = 10;
    var negTwoDigits = -10;

    var posThreeDigits = 100;
    var negThreeDigits = -100;

    var posTwoDigitsRet = utilities.twoDigits(posTwoDigits);
    var negTwoDigitsRet = utilities.twoDigits(negTwoDigits);
    var posThreeDigitsRet = utilities.twoDigits(posThreeDigits);
    var negThreeDigitsRet = utilities.twoDigits(negThreeDigits);

    posTwoDigitsRet.should.equal('10');
    negTwoDigitsRet.should.equal('-10');
    posThreeDigitsRet.should.equal('100');
    negThreeDigitsRet.should.equal('-100');
  });

  it('should return undefined when the parameter is not valid', function () {
    var str = 'str';
    var nullVal = null;
    var undefinedVal = undefined;

    var strRet = utilities.twoDigits(str);
    var nullValRet = utilities.twoDigits(null);
    var undefinedValRet = utilities.twoDigits(undefined);

    (typeof strRet).should.equal('undefined');
    (typeof nullValRet).should.equal('undefined');
    (typeof undefinedValRet).should.equal('undefined');
  });
});
