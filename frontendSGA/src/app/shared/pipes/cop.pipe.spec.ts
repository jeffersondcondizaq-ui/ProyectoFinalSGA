import { CopPipe } from './cop.pipe';

describe('CopPipe', () => {
  const pipe = new CopPipe();

  it('formatea pesos sin decimales', () => {
    expect(pipe.transform(250000)).toContain('250.000');
    expect(pipe.transform(250000)).not.toContain(',');
  });

  it('devuelve vacío para null', () => {
    expect(pipe.transform(null)).toBe('');
  });
});
